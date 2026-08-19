package com.beautyManager.beautyManagerApi.dto.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    private UUID id;
    private UUID userId;
    private Boolean appointmentReminders;
    private Boolean newClients;
    private Boolean cancellations;
    private Boolean monthlyReports;
    private Boolean systemUpdates;
}