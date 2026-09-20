package com.beautyManager.beautyManagerApi.service.authService;

import com.beautyManager.beautyManagerApi.dto.auth.AuthResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.LoginRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.UserSummaryDTO;
import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.BusinessRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import com.beautyManager.beautyManagerApi.security.JwtService;
import com.beautyManager.beautyManagerApi.service.sessionService.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserSessionService userSessionService;
    private final StaffRepository staffRepository;
    private final BusinessRepository businessRepository;

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
                .role(UserRole.Client)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);

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