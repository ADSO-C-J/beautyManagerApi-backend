package com.beautyManager.beautyManagerApi.service.reviewService;

import com.beautyManager.beautyManagerApi.dto.reviewDto.RatingStatsDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewRequestDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewResponseDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewUpdateDTO;
import com.beautyManager.beautyManagerApi.entity.AppointmentEntity;
import com.beautyManager.beautyManagerApi.entity.ClientEntity;
import com.beautyManager.beautyManagerApi.entity.ReviewEntity;
import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.AppointmentRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.ReviewRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findAll() {
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO findById(UUID id) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con id: " + id));
        return toDTO(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findByAppointmentId(UUID appointmentId) {
        return reviewRepository.findAllByAppointmentId(appointmentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findByClientId(UUID clientId) {
        return reviewRepository.findAllByClientId(clientId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findByStaffId(UUID staffId) {
        return reviewRepository.findAllByStaffIdOrderByCreatedAtDesc(staffId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingStatsDTO> getRatingStats() {
        // Agrupa por staffId y agrega promedio/total (solo reseñas públicas)
        Map<UUID, List<ReviewEntity>> byStaff = reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(r -> r.getStaffId() != null && Boolean.TRUE.equals(r.getIsPublic()))
                .collect(Collectors.groupingBy(ReviewEntity::getStaffId));

        return byStaff.entrySet().stream()
                .map(entry -> {
                    UUID staffId = entry.getKey();
                    List<ReviewEntity> reviews = entry.getValue();
                    double avg = reviews.stream().mapToInt(ReviewEntity::getRating).average().orElse(0);
                    return RatingStatsDTO.builder()
                            .staffId(staffId)
                            .staffName(resolveStaffName(staffId))
                            .averageRating(Math.round(avg * 10.0) / 10.0)
                            .totalReviews((long) reviews.size())
                            .build();
                })
                .collect(Collectors.toList());
    }
@Override
    @Transactional
    public ReviewResponseDTO create(ReviewRequestDTO dto) {
        AppointmentEntity appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("La cita no existe con id: " + dto.getAppointmentId()));

        ensureAppointmentCompleted(appointment);
        ensureClientExists(dto.getClientId());
        ensureNoDuplicateReview(dto.getAppointmentId());

        ReviewEntity review = ReviewEntity.builder()
                .appointmentId(dto.getAppointmentId())
                .clientId(dto.getClientId())
                .staffId(dto.getStaffId())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .isPublic(dto.getIsPublic())
                .build();

        return toDTO(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public ReviewResponseDTO update(UUID id, ReviewUpdateDTO dto) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con id: " + id));

        if (dto.getRating() != null) {
            review.setRating(dto.getRating());
        }
        if (dto.getComment() != null) {
            review.setComment(dto.getComment());
        }
        if (dto.getIsPublic() != null) {
            review.setIsPublic(dto.getIsPublic());
        }

        return toDTO(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con id: " + id));
        reviewRepository.delete(review);
    }

    private void ensureAppointmentCompleted(AppointmentEntity appointment) {
        if (appointment.getDeletedAt() != null) {
            throw new IllegalArgumentException("La cita asociada no está disponible");
        }
        if (!"completada".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden reseñar citas completadas (estado actual: " + appointment.getStatus() + ")");
        }
    }

    private void ensureClientExists(UUID clientId) {
        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("El cliente no existe con id: " + clientId));
        if (client.getDeletedAt() != null) {
            throw new ResourceNotFoundException("El cliente no existe con id: " + clientId);
        }
    }

    private void ensureNoDuplicateReview(UUID appointmentId) {
        reviewRepository.findByAppointmentId(appointmentId)
                .ifPresent(r -> {
                    throw new IllegalArgumentException("La cita " + appointmentId + " ya tiene una reseña registrada");
                });
    }

    private String resolveStaffName(UUID staffId) {
        StaffEntity staff = staffRepository.findById(staffId)
                .orElse(null);
        if (staff == null || staff.getUserId() == null) {
            return "Estilista";
        }
        return userRepository.findById(staff.getUserId())
                .map(User::getName)
                .orElse("Estilista");
    }

    private ReviewResponseDTO toDTO(ReviewEntity review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .appointmentId(review.getAppointmentId())
                .clientId(review.getClientId())
                .staffId(review.getStaffId())
                .rating(review.getRating())
                .comment(review.getComment())
                .isPublic(review.getIsPublic())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}