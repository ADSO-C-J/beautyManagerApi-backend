package com.beautyManager.beautyManagerApi.dto.staffScheduleDto;

import com.beautyManager.beautyManagerApi.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class StaffScheduleRequestDTO {

    @NotNull(message = "El staff es obligatorio")
    private java.util.UUID staffId;

    @NotNull(message = "El día es obligatorio")
    private DayOfWeek day;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startsAt;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endsAt;
}