package com.beautyManager.beautyManagerApi.service.stylistService;

import com.beautyManager.beautyManagerApi.dto.CreateStylistRequestDTO;
import com.beautyManager.beautyManagerApi.dto.StylistResponseDTO;
import com.beautyManager.beautyManagerApi.dto.UpdateStylistRequestDTO;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
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
public class StylistServiceImpl implements StylistService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<StylistResponseDTO> findAll() {
        return userRepository.findAllByRoleAndDeletedAtIsNull(UserRole.estilista)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    public StylistResponseDTO findById(UUID id) {
        User user = findActiveStylistOrThrow(id);
        return toDTO(user);
    }

    @Override
    public StylistResponseDTO create(CreateStylistRequestDTO dto) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())) {
            throw new IllegalArgumentException("Ya se encuentra registrado este email: " + dto.getEmail());
        }
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode("Estilista123"))  // clave temporal
                .role(UserRole.estilista)
                .isActive(true)
                .build();
        return toDTO(userRepository.save(user));
    }

    @Override
    public StylistResponseDTO update(UUID id, UpdateStylistRequestDTO dto) {
        User user = findActiveStylistOrThrow(id);
        validateEmailNotTaken(dto.getEmail(), id);

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());

        return toDTO(userRepository.save(user));
    }

    @Override
    public void delete(UUID id) {
        User user = findActiveStylistOrThrow(id);
        user.setDeletedAt(LocalDateTime.now()); // borrado lógico, consistente con el resto del proyecto
        userRepository.save(user);
    }

    private User findActiveStylistOrThrow(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .filter(u -> u.getRole() == UserRole.estilista)
                .orElseThrow(() -> new ResourceNotFoundException("Estilista no encontrado con id: " + id));
    }

    private void validateEmailNotTaken(String email, UUID currentUserId) {
        userRepository.findByEmailAndDeletedAtIsNull(email)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya se encuentra registrado este email: " + email);
                });
    }

    private StylistResponseDTO toDTO(User user) {
        StylistResponseDTO dto = new StylistResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setAvatarUrl(user.getAvatarUrl());
        // El registro de staff aporta el staffId (usado por /api/staff/{staffId}/...)
        // y la especialidad, que vive en la tabla staff y no en users.
        staffRepository.findByUserId(user.getId()).ifPresent(staff -> {
            dto.setStaffId(staff.getId());
            dto.setSpecialty(staff.getSpecialty());
        });
        return dto;
    }
}