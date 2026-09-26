package com.beautyManager.beautyManagerApi.service.auditLogService;

import com.beautyManager.beautyManagerApi.dto.auditLogDto.AuditLogPageDTO;
import com.beautyManager.beautyManagerApi.dto.auditLogDto.AuditLogResponseDTO;
import com.beautyManager.beautyManagerApi.entity.AuditLogEntity;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final int MAX_PAGE_SIZE = 200;

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public AuditLogPageDTO findAll(UUID userId, String tableName, String action, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        List<AuditLogEntity> all = fetchFiltered(userId, tableName, action);

        int total = all.size();
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<AuditLogResponseDTO> content = all.subList(from, to)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        int totalPages = safeSize == 0 ? 0 : (int) Math.ceil((double) total / safeSize);

        return AuditLogPageDTO.builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponseDTO findById(Long id) {
        AuditLogEntity log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de auditoría no encontrado con id: " + id));
        return toDTO(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findTableNames() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AuditLogEntity::getTableName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findActions() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AuditLogEntity::getAction)
                .filter(a -> a != null && !a.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private List<AuditLogEntity> fetchFiltered(UUID userId, String tableName, String action) {
        boolean hasUser = userId != null;
        boolean hasTable = tableName != null && !tableName.isBlank();
        boolean hasAction = action != null && !action.isBlank();

        if (hasUser && hasTable && hasAction) {
            return auditLogRepository.findAllByUserIdAndTableNameAndActionOrderByCreatedAtDesc(userId, tableName, action);
        }
        if (hasUser && hasTable) {
            return auditLogRepository.findAllByUserIdAndTableNameOrderByCreatedAtDesc(userId, tableName);
        }
        if (hasUser && hasAction) {
            return auditLogRepository.findAllByUserIdAndActionOrderByCreatedAtDesc(userId, action);
        }
        if (hasTable && hasAction) {
            return auditLogRepository.findAllByTableNameAndActionOrderByCreatedAtDesc(tableName, action);
        }
        if (hasUser) {
            return auditLogRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        }
        if (hasTable) {
            return auditLogRepository.findAllByTableNameOrderByCreatedAtDesc(tableName);
        }
        if (hasAction) {
            return auditLogRepository.findAllByActionOrderByCreatedAtDesc(action);
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    private AuditLogResponseDTO toDTO(AuditLogEntity e) {
        return AuditLogResponseDTO.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .action(e.getAction())
                .tableName(e.getTableName())
                .recordId(e.getRecordId())
                .oldValues(e.getOldValues())
                .newValues(e.getNewValues())
                .ipAddress(e.getIpAddress())
                .createdAt(e.getCreatedAt())
                .build();
    }
}