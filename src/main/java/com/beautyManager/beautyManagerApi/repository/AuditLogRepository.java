package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findAllByOrderByCreatedAtDesc();

    List<AuditLogEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<AuditLogEntity> findAllByTableNameOrderByCreatedAtDesc(String tableName);

    List<AuditLogEntity> findAllByActionOrderByCreatedAtDesc(String action);

    List<AuditLogEntity> findAllByUserIdAndTableNameOrderByCreatedAtDesc(UUID userId, String tableName);

    List<AuditLogEntity> findAllByUserIdAndActionOrderByCreatedAtDesc(UUID userId, String action);

    List<AuditLogEntity> findAllByTableNameAndActionOrderByCreatedAtDesc(String tableName, String action);

    List<AuditLogEntity> findAllByUserIdAndTableNameAndActionOrderByCreatedAtDesc(
            UUID userId, String tableName, String action);
}