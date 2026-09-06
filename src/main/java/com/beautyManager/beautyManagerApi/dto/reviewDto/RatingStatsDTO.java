package com.beautyManager.beautyManagerApi.dto.reviewDto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RatingStatsDTO {

    private UUID staffId;
    private String staffName;
    private Double averageRating;
    private Long totalReviews;
}