package com.beautyManager.beautyManagerApi.service.auditLogService;

import java.util.List;
import java.util.UUID;

public interface AuditLogService {

    /** Listado paginado con filtros opcionales (userId, tableName, action). */
    AuditLogPageDTO findAll(UUID userId, String tableName, String action, int page, int size);

    /** Detalle de un registro de auditoría. */
    AuditLogResponseDTO findById(Long id);

    /** Valores distintos de 'table_name' presentes en los logs (para poblar filtros). */
    List<String> findTableNames();

    /** Valores distintos de 'action' presentes en los logs. */
    List<String> findActions();
}