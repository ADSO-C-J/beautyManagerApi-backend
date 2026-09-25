package com.beautyManager.beautyManagerApi.dto.facialDto;


import jakarta.validation.constraints.NotBlank;

public record FacialRecommendationRequestDTO(
        @NotBlank String category,
        @NotBlank String title,
        @NotBlank String description
) {}