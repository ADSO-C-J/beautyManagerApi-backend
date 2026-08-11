package com.beautyManager.beautyManagerApi.service.appointmentService;

import com.beautyManager.beautyManagerApi.dto.AppointmentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateAppointmentRequestDTO;
import com.beautyManager.beautyManagerApi.entity.AppointmentEntity;
import com.beautyManager.beautyManagerApi.repository.AppointmentRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final UUID DEFAULT_BUSINESS_ID =
            UUID.fromString("b0000000-0000-0000-0000-000000000001");

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    @Override
    public List<AppointmentResponseDTO> findAll(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository
                .findAllByBusinessIdAndDeletedAtIsNullAndScheduledAtBetween(
                        DEFAULT_BUSINESS_ID, start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponseDTO create(CreateAppointmentRequestDTO dto) {
        LocalDateTime scheduledAt = LocalDateTime.parse(dto.getDate() + "T" + dto.getTime());
        LocalDateTime endsAt = scheduledAt.plusMinutes(60);

        AppointmentEntity entity = AppointmentEntity.builder()
                .businessId(DEFAULT_BUSINESS_ID)
                .clientId(dto.getClientId())
                .staffId(dto.getStaffId())
                .scheduledAt(scheduledAt)
                .endsAt(endsAt)
                .status("confirmada")
                .notes(dto.getService() + (dto.getNotes() != null ? " | " + dto.getNotes() : ""))
                .build();

        return toDTO(appointmentRepository.save(entity));
    }

    private AppointmentResponseDTO toDTO(AppointmentEntity appointment) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setClientId(appointment.getClientId());
        dto.setStylistId(appointment.getStaffId());
        dto.setScheduledAt(appointment.getScheduledAt());
        dto.setEndsAt(appointment.getEndsAt());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        if (appointment.getNotes() != null && appointment.getNotes().contains("|")) {
            dto.setService(appointment.getNotes().split("\\|")[0].trim());
        }
        clientRepository.findById(appointment.getClientId())
                .ifPresent(client -> dto.setClientName(client.getName()));
        if (appointment.getStaffId() != null) {
            staffRepository.findById(appointment.getStaffId())
                    .ifPresent(staff -> userRepository.findById(staff.getUserId())
                            .ifPresent(user -> dto.setStylistName(user.getName())));
        }
        return dto;
    }
}