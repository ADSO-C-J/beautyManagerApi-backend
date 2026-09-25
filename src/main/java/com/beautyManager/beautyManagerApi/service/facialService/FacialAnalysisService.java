package com.beautyManager.beautyManagerApi.service.facialService;

import com.beautyManager.beautyManagerApi.dto.facialDto.FacialAnalysisRequestDTO;
import com.beautyManager.beautyManagerApi.dto.facialDto.FacialAnalysisResponseDTO;
import com.beautyManager.beautyManagerApi.dto.facialDto.FacialRecommendationRequestDTO;
import com.beautyManager.beautyManagerApi.dto.facialDto.FacialRecommendationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FacialAnalysisService {
    List<FacialAnalysisResponseDTO> findByClient(UUID clientId);
    FacialAnalysisResponseDTO findById(UUID id);
    FacialAnalysisResponseDTO create(UUID clientId, FacialAnalysisRequestDTO dto);
    FacialRecommendationResponseDTO addRecommendation(UUID analysisId, FacialRecommendationRequestDTO dto);
    void delete(UUID id);
}