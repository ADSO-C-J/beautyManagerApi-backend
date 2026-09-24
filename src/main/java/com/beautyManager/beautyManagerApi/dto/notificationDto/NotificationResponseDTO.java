package com.beautyManager.beautyManagerApi.dto.notificationDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class NotificationResponseDTO {

    private UUID id;
    private String type;
    private String title;
    private String body;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}