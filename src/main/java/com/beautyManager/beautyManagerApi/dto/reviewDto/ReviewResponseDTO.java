package com.beautyManager.beautyManagerApi.dto.reviewDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponseDTO {

    private UUID id;
    private UUID appointmentId;
    private UUID clientId;
    private UUID staffId;
    private Short rating;
    private String comment;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}