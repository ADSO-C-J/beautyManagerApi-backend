package com.beautyManager.beautyManagerApi.service.userService;

import com.beautyManager.beautyManagerApi.dto.userDto.UserResponseDTO;
import com.beautyManager.beautyManagerApi.dto.userDto.UserRequestDTO;
import com.beautyManager.beautyManagerApi.entity.ClientEntity;
import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.ClientFrequency;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.BusinessRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** Comisión por defecto para una ficha de personal recién creada. */
    private static final BigDecimal DEFAULT_COMMISSION_PCT = new BigDecimal("15.00");

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final BusinessRepository businessRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return toDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + dto.getEmail());
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dto.getRole())
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        // Un estilista necesita ficha en la tabla staff: sin ella las citas fallan
        // con 404 ('Estilista no encontrado') al agendarlo.
        syncStaffRecord(saved);
        // Un cliente necesita ficha en la tabla clients: sin ella no aparece en el
        // módulo Clientes ni puede agendar citas (clients.id != users.id).
        syncClientRecord(saved);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public UserResponseDTO update(UUID id, UserRequestDTO dto) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        User saved = userRepository.save(user);
        // Cubre el cambio de rol hacia 'estilista' (requiere ficha) y la vuelta
        // desde 'estilista' (la ficha debe dejar de estar activa).
        syncStaffRecord(saved);
        // Cubre el cambio de rol hacia 'cliente' (requiere ficha en 'clients').
        syncClientRecord(saved);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        // Soft delete — no borra el registro, solo marca deleted_at
        user.setDeletedAt(LocalDateTime.now());
        user.setIsActive(false);
        userRepository.save(user);

        // Desactiva también su ficha de personal para que deje de aparecer en la
        // agenda y no queden citas asignadas a un estilista inactivo.
        deactivateStaffRecord(user.getId());
        // Igual con la ficha de cliente: si no, seguiría listado en módulo Clientes.
        deactivateClientRecord(user.getId());
    }

    /**
     * Mantiene la ficha de personal (tabla staff) coherente con el rol del usuario:
     *  - rol 'estilista': crea la ficha si no existe y la activa si estaba inactiva.
     *  - cualquier otro rol: desactiva la ficha, si la tuviera (p. ej. un estilista
     *    promovido a administrador).
     * Se ejecuta dentro de la misma transacción que el guardado del usuario, así que
     * un fallo aquí revierte el cambio de rol y no deja los datos desincronizados.
     */
    private void syncStaffRecord(User user) {
        if (user.getRole() == UserRole.estilista) {
            StaffEntity staff = staffRepository.findByUserId(user.getId()).orElse(null);

            if (staff == null) {
                staffRepository.save(StaffEntity.builder()
                        .userId(user.getId())
                        .businessId(resolveDefaultBusinessId())
                        .specialty("Sin asignar")
                        .commissionPct(DEFAULT_COMMISSION_PCT)
                        .isActive(Boolean.TRUE)
                        .hireDate(LocalDate.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
                return;
            }

            if (!Boolean.TRUE.equals(staff.getIsActive())) {
                staff.setIsActive(Boolean.TRUE);
                staff.setUpdatedAt(LocalDateTime.now());
                staffRepository.save(staff);
            }
            return;
        }

        deactivateStaffRecord(user.getId());
    }

    /** Desactiva la ficha de personal del usuario, si existe. */
    private void deactivateStaffRecord(UUID userId) {
        staffRepository.findByUserId(userId).ifPresent(staff -> {
            if (Boolean.TRUE.equals(staff.getIsActive())) {
                staff.setIsActive(Boolean.FALSE);
                staff.setUpdatedAt(LocalDateTime.now());
                staffRepository.save(staff);
            }
        });
    }

    /**
     * Garantiza que un usuario con rol 'cliente' tenga ficha en la tabla 'clients'.
     * Es el registro que lista el módulo Clientes del administrador y al que
     * apuntan las citas (clients.id != users.id); sin él el usuario registrado
     * no aparecía en ningún sitio.
     * Se ejecuta dentro de la misma transacción que el guardado del usuario.
     * Si el rol no es 'cliente' no se toca nada: el historial de citas previas
     * debe seguir visible.
     */
    private void syncClientRecord(User user) {
        if (user.getRole() != UserRole.cliente) {
            return;
        }
        // OJO: clients.user_id es UNIQUE, así que se comprueba también si la fila
        // existe borrada lógicamente; en ese caso no se crea otra (la decisión de
        // si debe reaparecer le corresponde al administrador, no a este método).
        if (clientRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }
        UUID businessId = resolveDefaultBusinessId();
        if (businessId == null) {
            // clients.business_id es NOT NULL: sin negocio no se puede crear la ficha.
            log.warn("No se creó la ficha de cliente para {}: no hay negocios configurados", user.getEmail());
            return;
        }
        clientRepository.save(ClientEntity.builder()
                .userId(user.getId())
                .businessId(businessId)
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .frequency(ClientFrequency.baja) // NOT NULL en la BD; cliente nuevo = baja
                .totalVisits(0)
                .totalSpent(BigDecimal.ZERO)
                .isActive(true)
                .build());
    }

    /** Borra (lógicamente) la ficha de cliente asociada al usuario eliminado. */
    private void deactivateClientRecord(UUID userId) {
        clientRepository.findByUserIdAndDeletedAtIsNull(userId).ifPresent(client -> {
            client.setDeletedAt(LocalDateTime.now());
            client.setIsActive(false);
            clientRepository.save(client);
        });
    }

    /** Negocio por defecto: el primero creado, igual que en el resto de servicios. */
    private UUID resolveDefaultBusinessId() {
        return businessRepository.findAllOrderedByCreation().stream()
                .findFirst()
                .map(business -> business.getId())
                .orElse(null);
    }

    private UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
