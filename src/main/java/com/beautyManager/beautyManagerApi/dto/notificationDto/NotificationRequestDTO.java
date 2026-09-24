package com.beautyManager.beautyManagerApi.dto.notificationDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequestDTO {

    @NotBlank(message = "El tipo es obligatorio")
    private String type;

    @NotBlank(message = "El título es obligatorio")
    private String title;

    private String body;

    private Map<String, Object> metadata;
}