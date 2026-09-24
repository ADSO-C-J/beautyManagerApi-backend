package com.beautyManager.beautyManagerApi.dto.auditLogDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponseDTO {

    private Long id;
    private UUID userId;
    private String action;
    private String tableName;
    private UUID recordId;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private String ipAddress;
    private LocalDateTime createdAt;
}