package com.beautyManager.beautyManagerApi.service.notificationService;

import com.beautyManager.beautyManagerApi.dto.notificationDto.NotificationRequestDTO;
import com.beautyManager.beautyManagerApi.dto.notificationDto.NotificationResponseDTO;
import com.beautyManager.beautyManagerApi.entity.NotificationEntity;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.NotificationRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findMyNotifications(String email) {
        UUID userId = resolveUserId(email);
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findMyUnreadNotifications(String email) {
        UUID userId = resolveUserId(email);
        return notificationRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnread(String email) {
        UUID userId = resolveUserId(email);
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(UUID id, String email) {
        UUID userId = resolveUserId(email);
        NotificationEntity notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return toDTO(notification);
    }

    @Override
    @Transactional
    public long markAllAsRead(String email) {
        UUID userId = resolveUserId(email);
        List<NotificationEntity> unread =
                notificationRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        LocalDateTime now = LocalDateTime.now();
        for (NotificationEntity n : unread) {
            n.setIsRead(true);
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Override
    @Transactional
    public void delete(UUID id, String email) {
        UUID userId = resolveUserId(email);
        NotificationEntity notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public NotificationResponseDTO create(NotificationRequestDTO dto, String email) {
        UUID userId = resolveUserId(email);
        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .type(dto.getType())
                .title(dto.getTitle())
                .body(dto.getBody())
                .metadata(dto.getMetadata())
                .isRead(false)
                .build();
        // El @CreationTimestamp se puebla en el flush; se fuerza un flush
        // para que la respuesta incluya createdAt sin un segundo viaje.
        NotificationEntity saved = notificationRepository.saveAndFlush(notification);
        return toDTO(saved);
    }

    private UUID resolveUserId(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(com.beautyManager.beautyManagerApi.entity.User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    private NotificationResponseDTO toDTO(NotificationEntity n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .metadata(n.getMetadata())
                .createdAt(n.getCreatedAt())
                .build();
    }
}