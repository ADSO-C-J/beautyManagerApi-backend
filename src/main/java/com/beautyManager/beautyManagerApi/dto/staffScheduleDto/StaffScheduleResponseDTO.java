package com.beautyManager.beautyManagerApi.dto.staffScheduleDto;

import com.beautyManager.beautyManagerApi.enums.DayOfWeek;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class StaffScheduleResponseDTO {

    private UUID id;
    private UUID staffId;
    private DayOfWeek day;
    private LocalTime startsAt;
    private LocalTime endsAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}