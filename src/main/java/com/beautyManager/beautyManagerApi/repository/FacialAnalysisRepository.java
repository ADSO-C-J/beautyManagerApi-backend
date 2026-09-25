package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.FacialAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FacialAnalysisRepository extends JpaRepository<FacialAnalysisEntity, UUID> {
    List<FacialAnalysisEntity> findAllByClientIdOrderByCreatedAtDesc(UUID clientId);
}