package com.beautyManager.beautyManagerApi.service.staffService;

import com.beautyManager.beautyManagerApi.dto.staffDto.StaffRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffDto.StaffResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffDto.StaffUpdateDTO;
import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponseDTO> findAll() {
        return staffRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponseDTO findById(UUID id) {
        return staffRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro del personal no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponseDTO> findByBusiness(UUID businessId) {
        return staffRepository.findAllByBusinessIdAndIsActiveTrue(businessId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StaffResponseDTO create(StaffRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + dto.getUserId()));

        // Regla de negocio: un usuario solo puede pertenecer a un registro de staff
        staffRepository.findByUserId(dto.getUserId()).ifPresent(s -> {
            throw new IllegalArgumentException("El usuario ya está registrado como miembro del personal");
        });

        StaffEntity staff = StaffEntity.builder()
                .userId(dto.getUserId())
                .businessId(dto.getBusinessId())
                .specialty(dto.getSpecialty())
                .bio(dto.getBio())
                .hireDate(dto.getHireDate())
                .commissionPct(dto.getCommissionPct() != null ? dto.getCommissionPct() : BigDecimal.ZERO)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        staff = staffRepository.save(staff);
        return toDTO(staff, user);
    }

    @Override
    @Transactional
    public StaffResponseDTO update(UUID id, StaffUpdateDTO dto) {
        StaffEntity staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro del personal no encontrado con id: " + id));

        if (dto.getSpecialty() != null) {
            staff.setSpecialty(dto.getSpecialty());
        }
        if (dto.getBio() != null) {
            staff.setBio(dto.getBio());
        }
        if (dto.getHireDate() != null) {
            staff.setHireDate(dto.getHireDate());
        }
        if (dto.getCommissionPct() != null) {
            if (dto.getCommissionPct().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("El porcentaje de comisión no puede ser mayor a 100");
            }
            staff.setCommissionPct(dto.getCommissionPct());
        }
        if (dto.getIsActive() != null) {
            staff.setIsActive(dto.getIsActive());
        }
        staff.setUpdatedAt(LocalDateTime.now());

        return toDTO(staffRepository.save(staff));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        StaffEntity staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro del personal no encontrado con id: " + id));
        // Borrado lógico: se desactiva para preservar el historial de citas
        staff.setIsActive(Boolean.FALSE);
        staff.setUpdatedAt(LocalDateTime.now());
        staffRepository.save(staff);
    }

    private StaffResponseDTO toDTO(StaffEntity staff) {
        User user = userRepository.findById(staff.getUserId()).orElse(null);
        return toDTO(staff, user);
    }

    private StaffResponseDTO toDTO(StaffEntity staff, User user) {
        return StaffResponseDTO.builder()
                .id(staff.getId())
                .userId(staff.getUserId())
                .userName(user != null ? user.getName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .businessId(staff.getBusinessId())
                .specialty(staff.getSpecialty())
                .bio(staff.getBio())
                .hireDate(staff.getHireDate())
                .commissionPct(staff.getCommissionPct())
                .isActive(staff.getIsActive())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
