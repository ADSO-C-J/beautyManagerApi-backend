package com.beautyManager.beautyManagerApi.dto.staffServiceDto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class StaffServiceAssignRequestDTO {

    @NotEmpty(message = "Debe enviar al menos un serviceId")
    private List<UUID> serviceIds;
}