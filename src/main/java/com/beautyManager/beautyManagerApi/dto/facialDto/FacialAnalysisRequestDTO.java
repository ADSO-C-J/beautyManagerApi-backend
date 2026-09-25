package com.beautyManager.beautyManagerApi.dto.facialDto;

// dto/facialDto/FacialAnalysisRequestDTO.java

import com.beautyManager.beautyManagerApi.enums.FaceShape;
import com.beautyManager.beautyManagerApi.enums.HairType;
import com.beautyManager.beautyManagerApi.enums.SkinTone;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record FacialAnalysisRequestDTO(
        String imageUrl,
        @NotNull SkinTone skinTone,
        String skinToneHex,
        @NotNull HairType hairType,
        @NotNull FaceShape faceShape,
        @DecimalMin("0") @DecimalMax("100") BigDecimal confidencePct,
        Map<String, Object> rawResult,
        List<FacialRecommendationRequestDTO> recommendations
) {}