package com.beautyManager.beautyManagerApi.repository;


import com.beautyManager.beautyManagerApi.entity.FacialRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FacialRecommendationRepository extends JpaRepository<FacialRecommendationEntity, UUID> {
    List<FacialRecommendationEntity> findAllByAnalysisId(UUID analysisId);
}