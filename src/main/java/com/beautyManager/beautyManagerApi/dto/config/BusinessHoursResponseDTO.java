package com.beautyManager.beautyManagerApi.dto.config;

import com.beautyManager.beautyManagerApi.enums.DayOfWeek;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class BusinessHoursResponseDTO {
    private UUID id;
    private UUID businessId;
    private DayOfWeek day;
    private LocalTime opensAt;
    private LocalTime closesAt;
    private Boolean isClosed;
}