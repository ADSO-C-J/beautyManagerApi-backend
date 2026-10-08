package com.beautyManager.beautyManagerApi.service.clientService;

import com.beautyManager.beautyManagerApi.dto.clientDto.ClientResponseDTO;
import com.beautyManager.beautyManagerApi.dto.clientDto.CreateClientRequestDTO;
import com.beautyManager.beautyManagerApi.dto.clientDto.UpdateClientRequestDTO;
import com.beautyManager.beautyManagerApi.entity.ClientEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.ClientFrequency;
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
import java.math.BigDecimal;
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

    /**
     * Indica si el usuario autenticado tiene rol administrador.
     *
     * El administrador es el unico perfil con vision GLOBAL: puede consultar
     * clientes de todos los negocios, no solo del suyo. El resto de roles
     * (estilista, recepcionista, cliente) queda acotado a su business_id.
     */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_administrador".equals(a.getAuthority()));
    }

    @Override
    public List<ClientResponseDTO> search(String query) {
        // Se lee de la tabla 'clients' para devolver el id que esperan
        // los módulos de citas/reseñas/pagos (clients.id != users.id).
        // El administrador ve TODOS los clientes; el resto, solo los de su negocio.
        List<ClientEntity> clients = isAdmin()
                ? clientRepository.findAllByDeletedAtIsNull()
                : clientRepository.findAllByBusinessIdAndDeletedAtIsNull(resolveBusinessId());
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
        // Se crea el usuario (para que el cliente pueda autenticarse) y el registro
        // de la tabla 'clients' (que es la fuente del listado y de las citas).
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode("CLiente123"))
                .role(UserRole.cliente)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);

        ClientEntity client = ClientEntity.builder()
                .userId(savedUser.getId())
                .businessId(resolveBusinessId())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .frequency(ClientFrequency.baja) // NOT NULL en la BD; cliente nuevo = baja
                .totalVisits(0)
                .totalSpent(BigDecimal.ZERO)
                .isActive(true)
                .build();
        return toDTO(clientRepository.save(client));
    }

    @Override
    public ClientResponseDTO update(UUID id, UpdateClientRequestDTO dto) {
        ClientEntity client = clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado por Id: " + id));
        validateEmailNotTaken(dto.getEmail(), client.getUserId());

        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());

        // Sincroniza el usuario asociado (si existe) para no dejarlos divergentes.
        if (client.getUserId() != null) {
            userRepository.findByIdAndDeletedAtIsNull(client.getUserId()).ifPresent(user -> {
                user.setName(dto.getName());
                user.setEmail(dto.getEmail());
                user.setPhone(dto.getPhone());
                userRepository.save(user);
            });
        }

        return toDTO(clientRepository.save(client));
    }

    @Override
    public ClientResponseDTO partialUpdate(UUID id, UpdateClientRequestDTO dto) {
        ClientEntity client = clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado por Id: " + id));

        // PATCH: solo se tocan los campos que vienen no-null.
        if (dto.getName() != null) {
            client.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            client.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            client.setPhone(dto.getPhone());
        }

        return toDTO(clientRepository.save(client));
    }

    @Override
    public void delete(UUID id) {
        // Borrado lógico sobre la misma entidad que lista/findById (tabla 'clients').
        ClientEntity client = clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado por Id: " + id));
        client.setDeletedAt(LocalDateTime.now());
        client.setIsActive(false);
        clientRepository.save(client);

        // Se desactiva también el usuario asociado para que no pueda iniciar sesión.
        if (client.getUserId() != null) {
            userRepository.findByIdAndDeletedAtIsNull(client.getUserId()).ifPresent(user -> {
                user.setIsActive(false);
                user.setDeletedAt(LocalDateTime.now());
                userRepository.save(user);
            });
        }
    }


    private ClientResponseDTO toDTO(ClientEntity client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        return dto;
    }

    private void validateEmailNotTaken(String email, UUID currentUserId) {
        userRepository.findByEmailAndDeletedAtIsNull(email)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya se encuentra registrado este email: " + email);
                });

    }}