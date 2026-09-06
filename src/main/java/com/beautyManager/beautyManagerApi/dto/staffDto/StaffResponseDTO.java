package com.beautyManager.beautyManagerApi.dto.staffDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class StaffResponseDTO {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID businessId;
    private String specialty;
    private String bio;
    private LocalDate hireDate;
    private BigDecimal commissionPct;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
