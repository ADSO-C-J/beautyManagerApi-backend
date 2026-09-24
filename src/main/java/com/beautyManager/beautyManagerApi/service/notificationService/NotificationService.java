package com.beautyManager.beautyManagerApi.service.notificationService;

import com.beautyManager.beautyManagerApi.dto.notificationDto.NotificationRequestDTO;
import com.beautyManager.beautyManagerApi.dto.notificationDto.NotificationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    /** Notificaciones del usuario autenticado (email del token). */
    List<NotificationResponseDTO> findMyNotifications(String email);

    /** Solo las no leídas del usuario autenticado. */
    List<NotificationResponseDTO> findMyUnreadNotifications(String email);

    /** Número de notificaciones no leídas (para el badge de la campana). */
    long countMyUnread(String email);

    /** Marca una notificación como leída. */
    NotificationResponseDTO markAsRead(UUID id, String email);

    /** Marca todas las notificaciones del usuario como leídas. Devuelve cuántas cambió. */
    long markAllAsRead(String email);

    /** Elimina una notificación del usuario. */
    void delete(UUID id, String email);

    /** Crea una notificación para el usuario autenticado (útil para pruebas/sistema). */
    NotificationResponseDTO create(NotificationRequestDTO dto, String email);
}