package com.beautyManager.beautyManagerApi.dto.facialDto;


import java.time.OffsetDateTime;
import java.util.UUID;

public record FacialRecommendationResponseDTO(
        UUID id,
        String category,
        String title,
        String description,
        OffsetDateTime createdAt
) {}