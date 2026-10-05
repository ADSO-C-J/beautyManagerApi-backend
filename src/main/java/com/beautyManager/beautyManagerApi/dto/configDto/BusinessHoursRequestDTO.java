package com.beautyManager.beautyManagerApi.dto.config;

import com.beautyManager.beautyManagerApi.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class BusinessHoursRequestDTO {

    @NotNull(message = "El día es obligatorio")
    private DayOfWeek day;

    private LocalTime opensAt;
    private LocalTime closesAt;

    private Boolean isClosed = false;
}