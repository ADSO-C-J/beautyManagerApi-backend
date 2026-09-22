package com.beautyManager.beautyManagerApi.service.clientService;

import com.beautyManager.beautyManagerApi.dto.ClientResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateClientRequestDTO;
import com.beautyManager.beautyManagerApi.dto.UpdateClientRequestDTO;
import com.beautyManager.beautyManagerApi.entity.ClientEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.BusinessRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    // Fallback por si no se puede resolver el negocio del usuario autenticado.
    private static final UUID DEFAULT_BUSINESS_ID =
            UUID.fromString("b0000000-0000-0000-0000-000000000001");

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Resuelve el negocio del usuario autenticado (staff -> su negocio; resto -> negocio por defecto).
     */
    private UUID resolveBusinessId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            UUID staffBusinessId = userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                    .flatMap(user -> staffRepository.findByUserId(user.getId()))
                    .map(staff -> staff.getBusinessId())
                    .orElse(null);
            if (staffBusinessId != null) {
                return staffBusinessId;
            }
        }
        return businessRepository.findAllOrderedByCreation().stream()
                .findFirst()
                .map(business -> business.getId())
                .orElse(DEFAULT_BUSINESS_ID);
    }

    @Override
    public List<ClientResponseDTO> search(String query) {
        // Se lee de la tabla 'clients' para devolver el id que esperan
        // los módulos de citas/reseñas/pagos (clients.id != users.id).
        List<ClientEntity> clients =
                clientRepository.findAllByBusinessIdAndDeletedAtIsNull(resolveBusinessId());
        if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(q))
                            || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }
        return clients.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ClientResponseDTO findById(UUID id){
        ClientEntity client = clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado por Id: " + id));
        return toDTO(client);
    }

    @Override
    public ClientResponseDTO create(CreateClientRequestDTO dto){
        if (userRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())){
            throw  new IllegalArgumentException("Ya se encuentra registrado este email"+ dto.getEmail());
        }
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode("CLiente123"))
                .role(UserRole.cliente)
                .isActive(true)
                .build();
        return toDTO(userRepository.save(user));
    }

    @Override
    public ClientResponseDTO update(UUID id, UpdateClientRequestDTO dto) {
        User user = findActiveClientOrThrow(id);
        validateEmailNotTaken(dto.getEmail(), id);

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());

        return toDTO(userRepository.save(user));
    }

    @Override
    public ClientResponseDTO partialUpdate(UUID id, UpdateClientRequestDTO dto) {
        User user = findActiveClientOrThrow(id);

        // PATCH: solo se tocan los campos que vienen no-null.
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        return toDTO(userRepository.save(user));
    }
    @Override
    public void delete(UUID id) {
        User user = findActiveClientOrThrow(id);
        user.setDeletedAt(LocalDateTime.now()); // borrado lógico, consistente con el resto del proyecto
        userRepository.save(user);
    }
    private User findActiveClientOrThrow(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .filter(u -> u.getRole() == UserRole.cliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado por Id: " + id));
    }


    private ClientResponseDTO toDTO(ClientEntity client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        return dto;
    }

    private ClientResponseDTO toDTO(User user) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        return dto;
    }

    private void validateEmailNotTaken(String email, UUID currentUserId) {
        userRepository.findByEmailAndDeletedAtIsNull(email)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya se encuentra registrado este email: " + email);
                });

    }}