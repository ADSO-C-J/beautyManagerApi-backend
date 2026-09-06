package com.beautyManager.beautyManagerApi.service.staffScheduleService;

import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleUpdateDTO;
import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import com.beautyManager.beautyManagerApi.entity.StaffScheduleEntity;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.StaffScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffScheduleServiceImpl implements StaffScheduleService {

    private final StaffScheduleRepository staffScheduleRepository;
    private final StaffRepository staffRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StaffScheduleResponseDTO> findAll() {
        return staffScheduleRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffScheduleResponseDTO> findByStaffId(UUID staffId) {
        return staffScheduleRepository.findByStaffIdOrderByDayAsc(staffId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffScheduleResponseDTO findById(UUID id) {
        StaffScheduleEntity schedule = staffScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado con id: " + id));
        return toDTO(schedule);
    }

    @Override
    @Transactional
    public StaffScheduleResponseDTO create(StaffScheduleRequestDTO dto) {
        ensureStaffExists(dto.getStaffId());
        validateTimeRange(dto.getStartsAt(), dto.getEndsAt());
        ensureNoDuplicateDay(dto.getStaffId(), dto.getDay(), null);

        StaffScheduleEntity schedule = StaffScheduleEntity.builder()
                .staffId(dto.getStaffId())
                .day(dto.getDay())
                .startsAt(dto.getStartsAt())
                .endsAt(dto.getEndsAt())
                .isActive(true)
                .build();

        return toDTO(staffScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public StaffScheduleResponseDTO update(UUID id, StaffScheduleUpdateDTO dto) {
        StaffScheduleEntity schedule = staffScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado con id: " + id));

        validateTimeRange(dto.getStartsAt(), dto.getEndsAt());

        schedule.setStartsAt(dto.getStartsAt());
        schedule.setEndsAt(dto.getEndsAt());
        if (dto.getIsActive() != null) {
            schedule.setIsActive(dto.getIsActive());
        }

        return toDTO(staffScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        StaffScheduleEntity schedule = staffScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado con id: " + id));
        staffScheduleRepository.delete(schedule);
    }

    private void ensureStaffExists(UUID staffId) {
        StaffEntity staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("El staff no existe con id: " + staffId));
        if (!Boolean.TRUE.equals(staff.getIsActive())) {
            throw new IllegalArgumentException("El staff está inactivo: " + staffId);
        }
    }

    private void ensureNoDuplicateDay(UUID staffId, com.beautyManager.beautyManagerApi.enums.DayOfWeek day, UUID ignoreId) {
        staffScheduleRepository.findByStaffIdAndDay(staffId, day)
                .filter(existing -> !existing.getId().equals(ignoreId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "El staff " + staffId + " ya tiene un horario configurado para el día " + day);
                });
    }

    private void validateTimeRange(java.time.LocalTime startsAt, java.time.LocalTime endsAt) {
        if (startsAt == null || endsAt == null) {
            throw new IllegalArgumentException("La hora de inicio y fin son obligatorias");
        }
        if (!startsAt.isBefore(endsAt)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private StaffScheduleResponseDTO toDTO(StaffScheduleEntity schedule) {
        return StaffScheduleResponseDTO.builder()
                .id(schedule.getId())
                .staffId(schedule.getStaffId())
                .day(schedule.getDay())
                .startsAt(schedule.getStartsAt())
                .endsAt(schedule.getEndsAt())
                .isActive(schedule.getIsActive())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}