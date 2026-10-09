package com.beautyManager.beautyManagerApi.service.appointmentService;

import com.beautyManager.beautyManagerApi.dto.appointmentDto.AppointmentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.appointmentDto.AppointmentServiceItemDTO;
import com.beautyManager.beautyManagerApi.dto.appointmentDto.CreateAppointmentRequestDTO;
import com.beautyManager.beautyManagerApi.dto.appointmentDto.UpdateAppointmentRequestDTO;
import com.beautyManager.beautyManagerApi.entity.AppointmentEntity;
import com.beautyManager.beautyManagerApi.entity.AppointmentServiceEntity;
import com.beautyManager.beautyManagerApi.enums.AppointmentStatus;
import com.beautyManager.beautyManagerApi.exception.InvalidRequestException;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.AppointmentRepository;
import com.beautyManager.beautyManagerApi.repository.AppointmentServiceRepository;
import com.beautyManager.beautyManagerApi.repository.BusinessRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.ServiceRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    // Fallback por si no se puede resolver el negocio del usuario autenticado.
    private static final UUID DEFAULT_BUSINESS_ID =
            UUID.fromString("b0000000-0000-0000-0000-000000000001");

    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Resuelve el negocio del usuario autenticado:
     *  - Si el usuario pertenece al staff, devuelve el business_id de su registro.
     *  - En caso contrario, devuelve el negocio por defecto (el primero creado).
     *  - Si no hay sesión o no hay negocio configurado, usa el fallback.
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
    public List<AppointmentResponseDTO> findAll(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository
                .findAllByBusinessIdAndDeletedAtIsNullAndScheduledAtBetween(
                        resolveBusinessId(), start, end)
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
        // Validar referencias antes de guardar para no disparar una violación de
        // clave foránea (que se propagaba a /error y se enmascaraba como 401).
        if (dto.getClientId() == null || !clientRepository.existsById(dto.getClientId())) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + dto.getClientId());
        }
        if (dto.getStaffId() == null || !staffRepository.existsById(dto.getStaffId())) {
            throw new ResourceNotFoundException("Estilista no encontrado con id: " + dto.getStaffId());
        }

        LocalDateTime scheduledAt = parseScheduledAt(dto.getDate(), dto.getTime());

        AppointmentEntity entity = AppointmentEntity.builder()
                .businessId(resolveBusinessId())
                .clientId(dto.getClientId())
                .staffId(dto.getStaffId())
                .scheduledAt(scheduledAt)
                .endsAt(scheduledAt.plusMinutes(60))
                .status(AppointmentStatus.confirmada)
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
        appt.setNotes(buildNotes(dto.getService(), dto.getNotes()));

        // Coherencia con el CHECK "cancelled_fields_consistent": si el estado pasa a
        // 'cancelada' hay que rellenar cancelled_at (y cancelled_by); al revés,
        // cualquier otro estado debe limpiar esos campos.
        AppointmentStatus newStatus = parseStatus(dto.getStatus());
        appt.setStatus(newStatus);
        if (newStatus == AppointmentStatus.cancelada) {
            if (appt.getCancelledAt() == null) {
                appt.setCancelledAt(LocalDateTime.now());
            }
            if (appt.getCancelledBy() == null) {
                appt.setCancelledBy(currentUserId());
            }
        } else {
            appt.setCancelledAt(null);
            appt.setCancelledBy(null);
            appt.setCancellationReason(null);
        }

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
        dto.setStatus(appointment.getStatus() != null ? appointment.getStatus().name() : null);
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
        attachServices(dto, appointment.getId());
        return dto;
    }

    /**
     * Completa en el DTO la lista de servicios de la cita (tabla appointment_services)
     * junto con los totales de precio y duración.
     */
    private void attachServices(AppointmentResponseDTO dto, UUID appointmentId) {
        List<AppointmentServiceEntity> items =
                appointmentServiceRepository.findAllByAppointmentId(appointmentId);
        if (items.isEmpty()) {
            dto.setServices(List.of());
            dto.setTotalPrice(BigDecimal.ZERO);
            dto.setTotalDurationMin(0);
            return;
        }

        List<AppointmentServiceItemDTO> services = items.stream()
                .map(item -> {
                    var svc = serviceRepository.findById(item.getServiceId()).orElse(null);
                    return AppointmentServiceItemDTO.builder()
                            .id(item.getId())
                            .serviceId(item.getServiceId())
                            .name(svc != null ? svc.getName() : null)
                            .category(svc != null ? svc.getCategory() : null)
                            .priceAtTime(item.getPriceAtTime())
                            .durationAtTime(item.getDurationAtTime())
                            .build();
                })
                .collect(Collectors.toList());

        dto.setServices(services);
        dto.setTotalPrice(items.stream()
                .map(AppointmentServiceEntity::getPriceAtTime)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setTotalDurationMin(items.stream()
                .map(AppointmentServiceEntity::getDurationAtTime)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum());
    }

    /**
     * Convierte date + time (strings del DTO) a LocalDateTime.
     * Tolera variantes habituales del cliente:
     *  - hora con un solo dígito: "8:00" / "9:30" -> "08:00" / "09:30"
     *  - sin segundos: "08:00" -> "08:00:00"
     *  - con segundos completos: "08:00:00" (se usa tal cual)
     */
    private LocalDateTime parseScheduledAt(String date, String time) {
        try {
            String d = date == null ? "" : date.trim();
            String t = time == null ? "" : time.trim();

            // Normaliza la hora a HH:mm[:ss] con dos dígitos en la hora.
            if (t.matches("\\d{1,2}:\\d{2}")) {
                t = t + ":00";
            }
            if (t.matches("\\d{1,2}:.*")) {
                String[] parts = t.split(":", 2);
                t = String.format("%02d", Integer.parseInt(parts[0])) + ":" + parts[1];
            }

            return LocalDateTime.parse(d + "T" + t);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new InvalidRequestException("Formato de fecha/hora inválido: " + date + "T" + time);
        }
    }

    private String buildNotes(String service, String notes) {
        return service + (notes != null ? " | " + notes : "");
    }

    /** Convierte el status recibido (String del DTO) al enum de la BD. */
    private AppointmentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return AppointmentStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Estado de cita inválido: " + status);
        }
    }

    /** Id del usuario autenticado (claim del contexto de seguridad), o null si no hay sesión. */
    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .map(user -> user.getId())
                .orElse(null);
    }
}