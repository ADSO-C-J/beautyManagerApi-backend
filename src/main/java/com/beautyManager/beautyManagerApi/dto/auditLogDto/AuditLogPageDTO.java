package com.beautyManager.beautyManagerApi.dto.auditLogDto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Página de resultados de auditoría. La paginación se hace en el servicio
 * (el proyecto no usa Spring Data Web/Pageable en ningún endpoint).
 */
@Data
@Builder
public class AuditLogPageDTO {

    private List<AuditLogResponseDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}