package com.beautyManager.beautyManagerApi.service.appointmentService;

import com.beautyManager.beautyManagerApi.dto.AppointmentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateAppointmentRequestDTO;
import com.beautyManager.beautyManagerApi.dto.UpdateAppointmentRequestDTO;
import com.beautyManager.beautyManagerApi.entity.AppointmentEntity;
import com.beautyManager.beautyManagerApi.exception.InvalidRequestException;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.AppointmentRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
    public AppointmentResponseDTO findById(UUID id) {
        AppointmentEntity appt = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));
        return toDTO(appt);
    }

    @Override
    public AppointmentResponseDTO create(CreateAppointmentRequestDTO dto) {
        LocalDateTime scheduledAt = parseScheduledAt(dto.getDate(), dto.getTime());

        AppointmentEntity entity = AppointmentEntity.builder()
                .businessId(DEFAULT_BUSINESS_ID)
                .clientId(dto.getClientId())
                .staffId(dto.getStaffId())
                .scheduledAt(scheduledAt)
                .endsAt(scheduledAt.plusMinutes(60))
                .status("confirmada")
                .notes(buildNotes(dto.getService(), dto.getNotes()))
                .build();

        return toDTO(appointmentRepository.save(entity));
    }

    @Override
    public AppointmentResponseDTO update(UUID id, UpdateAppointmentRequestDTO dto) {
        AppointmentEntity appt = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));

        LocalDateTime scheduledAt = parseScheduledAt(dto.getDate(), dto.getTime());
        appt.setScheduledAt(scheduledAt);
        appt.setEndsAt(scheduledAt.plusMinutes(60));
        appt.setStaffId(dto.getStaffId());
        appt.setStatus(dto.getStatus());
        appt.setNotes(buildNotes(dto.getService(), dto.getNotes()));

        return toDTO(appointmentRepository.save(appt));
    }

    @Override
    public void delete(UUID id) {
        AppointmentEntity appt = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));

        appt.setDeletedAt(LocalDateTime.now());
        appointmentRepository.save(appt);
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

    private LocalDateTime parseScheduledAt(String date, String time) {
        try {
            return LocalDateTime.parse(date + "T" + time);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Formato de fecha/hora inválido: " + date + "T" + time);
        }
    }

    private String buildNotes(String service, String notes) {
        return service + (notes != null ? " | " + notes : "");
    }
}