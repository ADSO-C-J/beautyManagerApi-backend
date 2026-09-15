package com.beautyManager.beautyManagerApi.service.clientService;

import com.beautyManager.beautyManagerApi.dto.ClientResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateClientRequestDTO;
import com.beautyManager.beautyManagerApi.dto.UpdateClientRequestDTO;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClientResponseDTO> search(String query) {
        List<User> users = userRepository.findAllByRoleAndDeletedAtIsNull(UserRole.Client);
        if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> u.getName().toLowerCase().contains(q)
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ClientResponseDTO findById(UUID id){
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .filter(u -> u.getRole() == UserRole.Client)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado por Id: " + id));
        return toDTO(user);
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
                .role(UserRole.Client)
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
                .filter(u -> u.getRole() == UserRole.Client)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado por Id: " + id));
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