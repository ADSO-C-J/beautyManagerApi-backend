package com.beautyManager.beautyManagerApi.service.authService;

import com.beautyManager.beautyManagerApi.dto.auth.AuthResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.LoginRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RefreshRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.UserSummaryDTO;
import com.beautyManager.beautyManagerApi.entity.ClientEntity;
import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.entity.UserSessionEntity;
import com.beautyManager.beautyManagerApi.enums.ClientFrequency;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import com.beautyManager.beautyManagerApi.exception.InvalidRequestException;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.BusinessRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import com.beautyManager.beautyManagerApi.security.JwtService;
import com.beautyManager.beautyManagerApi.service.sessionService.UserSessionService;
import com.beautyManager.beautyManagerApi.service.tokenService.RevokedTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Fallback por si no se puede resolver el negocio del usuario autenticado.
    private static final UUID DEFAULT_BUSINESS_ID =
            UUID.fromString("b0000000-0000-0000-0000-000000000001");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserSessionService userSessionService;
    private final RevokedTokenService revokedTokenService;
    private final StaffRepository staffRepository;
    private final BusinessRepository businessRepository;
    private final ClientRepository clientRepository;

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto, String ipAddress, String userAgent) {
        // 1. Buscar al usuario por email (que no esté borrado)
        User user = userRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // 2. Verificar que esté activo
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("El usuario está inactivo");
        }

        // 3. Verificar la contraseña con BCrypt
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // 4. Actualizar la fecha del último acceso
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Resolver el negocio del usuario (staff -> su negocio; resto -> negocio por defecto)
        UUID businessId = resolveBusinessId(user);

        // 6. Generar token y armar la respuesta
        String token = jwtService.generateToken(user, businessId != null ? businessId.toString() : null);
        String roleAuthority = "ROLE_" + user.getRole().name();

        // 7. Registrar la sesión con un refresh token (se guarda solo el hash SHA-256)
        String refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        long expiresInMillis = jwtService.getExpirationMs();
        userSessionService.createSession(
                user.getId(),
                refreshToken,
                LocalDateTime.now().plusNanos(expiresInMillis * 1_000_000L),
                (ipAddress == null || ipAddress.isBlank()) ? null : ipAddress,
                (userAgent == null || userAgent.isBlank()) ? null : userAgent);

        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(expiresInMillis)
                .tokenType("Bearer")
                .roles(List.of(roleAuthority))
                .user(toUserSummary(user, businessId))
                .build();
    }

    @Override
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + dto.getEmail());
        }

        // Buenas prácticas: el registro público siempre crea el rol 'cliente',
        // los roles administrativos se asignan vía gestión de usuarios/semilla.
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(UserRole.cliente)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);

        // Además del usuario (que solo sirve para autenticarse) se crea la ficha en
        // la tabla 'clients': es la fuente del módulo Clientes del administrador y
        // el id que usan citas, análisis faciales y reseñas (clients.id != users.id).
        // Sin esta fila el cliente registrado no aparecía en ningún listado.
        clientRepository.save(ClientEntity.builder()
                .userId(saved.getId())
                .businessId(resolveClientBusinessId(saved))
                .name(saved.getName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .frequency(ClientFrequency.baja) // NOT NULL en la BD; cliente nuevo = baja
                .totalVisits(0)
                .totalSpent(BigDecimal.ZERO)
                .isActive(true)
                .build());

        return RegisterResponseDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    @Override
    public UserSummaryDTO getCurrentUser(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return toUserSummary(user, resolveBusinessId(user));
    }

    @Override
    @Transactional
    public AuthResponseDTO refresh(RefreshRequestDTO dto, String ipAddress, String userAgent) {
        // 1. Localizar la sesión por el hash del refresh token recibido.
        UserSessionEntity session = userSessionService
                .findByRawRefreshToken(dto.getRefreshToken())
                .orElseThrow(() -> new InvalidRequestException("Refresh token inválido"));

        // 2. Rechazar sesiones expiradas.
        if (session.getExpiresAt() == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSessionService.deleteSession(session);
            throw new InvalidRequestException("El refresh token ha expirado");
        }

        // 3. Cargar el usuario dueño de la sesión y validar que siga activo.
        User user = userRepository.findByIdAndDeletedAtIsNull(session.getUserId())
                .orElseThrow(() -> new InvalidRequestException("Usuario no encontrado"));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidRequestException("El usuario está inactivo");
        }

        // 4. Rotación de refresh token: se elimina el anterior y se emite uno nuevo.
        userSessionService.deleteSession(session);

        UUID businessId = resolveBusinessId(user);
        String token = jwtService.generateToken(user, businessId != null ? businessId.toString() : null);
        String newRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        long expiresInMillis = jwtService.getExpirationMs();
        userSessionService.createSession(
                user.getId(),
                newRefreshToken,
                LocalDateTime.now().plusNanos(expiresInMillis * 1_000_000L),
                (ipAddress == null || ipAddress.isBlank()) ? null : ipAddress,
                (userAgent == null || userAgent.isBlank()) ? null : userAgent);

        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(newRefreshToken)
                .expiresIn(expiresInMillis)
                .tokenType("Bearer")
                .roles(List.of("ROLE_" + user.getRole().name()))
                .user(toUserSummary(user, businessId))
                .build();
    }

    @Override
    public boolean logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        try {
            String jti = jwtService.extractJti(token);
            if (jti == null) {
                return false;
            }
            long expiresAtMillis = jwtService.extractExpiration(token).getTime();
            UUID userId = parseUserId(jwtService.extractUserId(token));

            // Guardamos el jti hasta su expiración: el filtro JWT lo rechazará.
            revokedTokenService.revoke(jti, userId, expiresAtMillis);
            return true;
        } catch (Exception ex) {
            // Token malformado o expirado: no hay nada que revocar.
            return false;
        }
    }

    private UUID parseUserId(String rawUserId) {
        if (rawUserId == null) {
            return null;
        }
        try {
            return UUID.fromString(rawUserId);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Resuelve el negocio asociado al usuario:
     *  - Si el usuario pertenece al staff, devuelve el business_id de su registro de staff.
     *  - En caso contrario (p. ej. administrador sin staff o cliente), devuelve el
     *    negocio por defecto (el primero creado).
     */
    private UUID resolveBusinessId(User user) {
        return staffRepository.findByUserId(user.getId())
                .map(StaffEntity::getBusinessId)
                .orElseGet(() -> businessRepository.findAllOrderedByCreation().stream()
                        .findFirst()
                        .map(b -> b.getId())
                        .orElse(null));
    }

    /**
     * Negocio al que asignar la ficha de un cliente recién registrado: el del staff
     * si lo hubiera; si no, el primero creado; y como último recurso el negocio por
     * defecto (la columna clients.business_id es NOT NULL, no se puede dejar vacía).
     */
    private UUID resolveClientBusinessId(User user) {
        UUID businessId = resolveBusinessId(user);
        return businessId != null ? businessId : DEFAULT_BUSINESS_ID;
    }

    private UserSummaryDTO toUserSummary(User user, UUID businessId) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .businessId(businessId)
                .build();
    }
}