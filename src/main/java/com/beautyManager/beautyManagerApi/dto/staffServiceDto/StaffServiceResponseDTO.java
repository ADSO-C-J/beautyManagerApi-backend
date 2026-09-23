package com.beautyManager.beautyManagerApi.dto.staffServiceDto;

import com.beautyManager.beautyManagerApi.enums.TypeServices;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class StaffServiceResponseDTO {

    private UUID staffId;
    private String staffName;
    private UUID serviceId;
    private String serviceName;
    private TypeServices serviceCategory;
    private BigDecimal servicePrice;
    private Integer serviceDurationMin;
    private LocalDateTime createdAt;
}