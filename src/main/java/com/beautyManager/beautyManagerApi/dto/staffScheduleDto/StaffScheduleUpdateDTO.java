package com.beautyManager.beautyManagerApi.dto.staffScheduleDto;

import com.beautyManager.beautyManagerApi.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class StaffScheduleUpdateDTO {

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startsAt;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endsAt;

    private Boolean isActive;
}