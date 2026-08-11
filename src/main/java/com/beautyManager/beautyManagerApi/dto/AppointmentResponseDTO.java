package com.beautyManager.beautyManagerApi.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class AppointmentResponseDTO {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private UUID stylistId;
    private String stylistName;
    private String service;
    private LocalDateTime scheduledAt;
    private LocalDateTime endsAt;
    private String status;
    private String notes;
}