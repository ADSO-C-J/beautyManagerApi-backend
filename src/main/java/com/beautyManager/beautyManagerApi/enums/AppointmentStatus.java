package com.beautyManager.beautyManagerApi.enums;

/**
 * Valores del ENUM nativo de PostgreSQL `appointment_status`.
 * Debe coincidir exactamente con el tipo de la BD; se mapea con
 * {@code @JdbcTypeCode(SqlTypes.NAMED_ENUM)} en la entidad.
 */
public enum AppointmentStatus {
    pendiente,
    confirmada,
    en_proceso,
    completada,
    cancelada,
    no_presentado
}
