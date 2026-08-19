package com.beautyManager.beautyManagerApi.service.businessHoursService;

import com.beautyManager.beautyManagerApi.dto.config.BusinessHoursRequestDTO;
import com.beautyManager.beautyManagerApi.dto.config.BusinessHoursResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BusinessHoursService {

    List<BusinessHoursResponseDTO> findByBusinessId(UUID businessId);

    BusinessHoursResponseDTO upsert(UUID businessId, BusinessHoursRequestDTO dto);
}