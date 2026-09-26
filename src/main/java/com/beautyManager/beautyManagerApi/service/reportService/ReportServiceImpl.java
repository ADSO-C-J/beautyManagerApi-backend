package com.beautyManager.beautyManagerApi.service.reportService;

import com.beautyManager.beautyManagerApi.dto.reportDto.MonthlyRevenueDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.ReportMetricsDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.ServicePopularityDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.StaffPerformanceDTO;
import com.beautyManager.beautyManagerApi.entity.AppointmentServiceEntity;
import com.beautyManager.beautyManagerApi.entity.PaymentEntity;
import com.beautyManager.beautyManagerApi.entity.ServiceEntity;
import com.beautyManager.beautyManagerApi.entity.StaffPerformanceViewEntity;
import com.beautyManager.beautyManagerApi.enums.PaymentStatus;
import com.beautyManager.beautyManagerApi.repository.AppointmentRepository;
import com.beautyManager.beautyManagerApi.repository.AppointmentServiceRepository;
import com.beautyManager.beautyManagerApi.repository.BusinessRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.PaymentRepository;
import com.beautyManager.beautyManagerApi.repository.ReportRepository;
import com.beautyManager.beautyManagerApi.repository.ServiceRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final UUID DEFAULT_BUSINESS_ID =
            UUID.fromString("b0000000-0000-0000-0000-000000000001");

    private final ReportRepository reportRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final ClientRepository clientRepository;
    private final PaymentRepository paymentRepository;
    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    /** Resuelve el negocio del usuario autenticado o el primero creado (mismo criterio que citas). */
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

    /** Convierte el rango solicitado por el frontend en una fecha de inicio. */
    private LocalDateTime startOfRange(String range) {
        LocalDate today = LocalDate.now();
        if (range == null) {
            return today.withDayOfMonth(1).atStartOfDay();
        }
        return switch (range.toLowerCase()) {
            case "week" -> today.minusDays(6).atStartOfDay();
            case "quarter" -> today.minusMonths(2).withDayOfMonth(1).atStartOfDay();
            case "year" -> today.withDayOfYear(1).atStartOfDay();
            default -> today.withDayOfMonth(1).atStartOfDay(); // month
        };
    }

    @Override
    @Transactional(readOnly = true)
    public ReportMetricsDTO getMetrics(String range) {
        UUID businessId = resolveBusinessId();
        LocalDateTime from = startOfRange(range);
        LocalDateTime to = LocalDateTime.now();

        long totalAppointments = appointmentRepository.countByBusinessIdAndDeletedAtIsNull(businessId);
        long cancelled = appointmentRepository
                .countByBusinessIdAndStatusNative(businessId, "cancelada");
        long noShow = appointmentRepository
                .countByBusinessIdAndStatusNative(businessId, "no_presentado");
        long newClients = clientRepository
                .countByBusinessIdAndDeletedAtIsNullAndCreatedAtBetween(businessId, from, to);
        BigDecimal revenue = paymentRepository
                .findAllByStatusAndPaidAtIsNotNullOrderByPaidAtAsc(PaymentStatus.pagado)
                .stream()
                .filter(p -> p.getPaidAt().isAfter(from) && p.getPaidAt().isBefore(to))
                .map(PaymentEntity::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double cancellationRate = totalAppointments == 0
                ? 0.0
                : (cancelled + noShow) * 100.0 / totalAppointments;

        return ReportMetricsDTO.builder()
                .totalRevenue(revenue)
                .totalAppointments(totalAppointments)
                .newClients(newClients)
                .cancellationRate(Math.round(cancellationRate * 10.0) / 10.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueDTO> getMonthlyRevenue() {
        // Agrupa los pagos por mes (mismo criterio que ReviewServiceImpl.getRatingStats)
        Map<Integer, BigDecimal> byMonth = paymentRepository
                .findAllByStatusAndPaidAtIsNotNullOrderByPaidAtAsc(PaymentStatus.pagado)
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaidAt().getMonthValue(),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, PaymentEntity::getAmount, BigDecimal::add)));

        return byMonth.entrySet().stream()
                .map(entry -> MonthlyRevenueDTO.builder()
                        .month(entry.getKey())
                        .revenue(entry.getValue() == null ? BigDecimal.ZERO : entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePopularityDTO> getServicePopularity() {
        // Agrupa por servicio y agrega reservas/ingresos (mismo criterio que ReviewServiceImpl.getRatingStats)
        Map<UUID, List<AppointmentServiceEntity>> byService = appointmentServiceRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(AppointmentServiceEntity::getServiceId));

        return byService.entrySet().stream()
                .map(entry -> {
                    UUID serviceId = entry.getKey();
                    List<AppointmentServiceEntity> bookings = entry.getValue();
                    ServiceEntity service = serviceRepository.findById(serviceId).orElse(null);
                    BigDecimal revenue = bookings.stream()
                            .map(AppointmentServiceEntity::getPriceAtTime)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return ServicePopularityDTO.builder()
                            .serviceName(service != null ? service.getName() : null)
                            .category(service != null ? service.getCategory() : null)
                            .bookingCount((long) bookings.size())
                            .revenue(revenue)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getBookingCount(), a.getBookingCount()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffPerformanceDTO> getStaffPerformance() {
        return reportRepository.findAllByOrderByTotalRevenueDesc()
                .stream()
                .map(this::toStaffPerformanceDTO)
                .collect(Collectors.toList());
    }

    private StaffPerformanceDTO toStaffPerformanceDTO(StaffPerformanceViewEntity view) {
        return StaffPerformanceDTO.builder()
                .staffId(view.getStaffId())
                .staffName(view.getStaffName())
                .specialty(view.getSpecialty())
                .totalAppointments(view.getTotalAppointments() == null ? 0L : view.getTotalAppointments())
                .totalRevenue(view.getTotalRevenue() == null ? BigDecimal.ZERO : view.getTotalRevenue())
                .avgRating(view.getAvgRating())
                .totalReviews(view.getTotalReviews() == null ? 0L : view.getTotalReviews())
                .build();
    }
}