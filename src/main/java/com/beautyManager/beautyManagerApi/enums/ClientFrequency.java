package com.beautyManager.beautyManagerApi.enums;

/**
 * Valores del ENUM nativo de PostgreSQL `client_frequency`.
 * Debe coincidir exactamente con el tipo de la BD; se mapea con
 * {@code @JdbcTypeCode(SqlTypes.NAMED_ENUM)} en la entidad.
 */
public enum ClientFrequency {
    alta,
    media,
    baja
}
