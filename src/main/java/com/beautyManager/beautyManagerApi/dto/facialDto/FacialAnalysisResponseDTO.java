package com.beautyManager.beautyManagerApi.dto.facialDto;

// dto/facialDto/FacialAnalysisResponseDTO.java

import com.beautyManager.beautyManagerApi.enums.FaceShape;
import com.beautyManager.beautyManagerApi.enums.HairType;
import com.beautyManager.beautyManagerApi.enums.SkinTone;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record FacialAnalysisResponseDTO(
        UUID id,
        UUID clientId,
        String clientName,
        UUID staffId,
        String staffName,
        String imageUrl,
        SkinTone skinTone,
        String skinToneHex,
        HairType hairType,
        FaceShape faceShape,
        BigDecimal confidencePct,
        Map<String, Object> rawResult,
        OffsetDateTime createdAt,
        List<FacialRecommendationResponseDTO> recommendations
) {}