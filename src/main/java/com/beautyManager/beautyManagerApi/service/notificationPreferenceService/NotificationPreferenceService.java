package com.beautyManager.beautyManagerApi.service.notificationPreferenceService;

import com.beautyManager.beautyManagerApi.dto.config.NotificationPreferenceDTO;

import java.util.UUID;

public interface NotificationPreferenceService {

    NotificationPreferenceDTO getByUserId(UUID userId);

    NotificationPreferenceDTO update(UUID userId, NotificationPreferenceDTO dto);
}