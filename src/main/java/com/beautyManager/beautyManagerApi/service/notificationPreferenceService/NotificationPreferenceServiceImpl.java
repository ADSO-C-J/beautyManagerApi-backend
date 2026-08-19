package com.beautyManager.beautyManagerApi.service.notificationPreferenceService;

import com.beautyManager.beautyManagerApi.dto.config.NotificationPreferenceDTO;
import com.beautyManager.beautyManagerApi.entity.NotificationPreferenceEntity;
import com.beautyManager.beautyManagerApi.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Override
    public NotificationPreferenceDTO getByUserId(UUID userId) {
        NotificationPreferenceEntity entity = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));
        return toDTO(entity);
    }

    @Override
    public NotificationPreferenceDTO update(UUID userId, NotificationPreferenceDTO dto) {
        NotificationPreferenceEntity entity = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));

        entity.setAppointmentReminders(dto.getAppointmentReminders());
        entity.setNewClients(dto.getNewClients());
        entity.setCancellations(dto.getCancellations());
        entity.setMonthlyReports(dto.getMonthlyReports());
        entity.setSystemUpdates(dto.getSystemUpdates());

        return toDTO(preferenceRepository.save(entity));
    }

    private NotificationPreferenceEntity createDefault(UUID userId) {
        NotificationPreferenceEntity entity = NotificationPreferenceEntity.builder()
                .userId(userId)
                .appointmentReminders(true)
                .newClients(true)
                .cancellations(true)
                .monthlyReports(false)
                .systemUpdates(false)
                .build();
        return preferenceRepository.save(entity);
    }

    private NotificationPreferenceDTO toDTO(NotificationPreferenceEntity e) {
        return NotificationPreferenceDTO.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .appointmentReminders(e.getAppointmentReminders())
                .newClients(e.getNewClients())
                .cancellations(e.getCancellations())
                .monthlyReports(e.getMonthlyReports())
                .systemUpdates(e.getSystemUpdates())
                .build();
    }
}