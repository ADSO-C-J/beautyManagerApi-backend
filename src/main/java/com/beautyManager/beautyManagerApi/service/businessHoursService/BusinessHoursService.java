package com.beautyManager.beautyManagerApi.service.businessHoursService;

import com.beautyManager.beautyManagerApi.dto.configDto.BusinessHoursRequestDTO;
import com.beautyManager.beautyManagerApi.dto.configDto.BusinessHoursResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BusinessHoursService {

    List<BusinessHoursResponseDTO> findByBusinessId(UUID businessId);

    BusinessHoursResponseDTO upsert(UUID businessId, BusinessHoursRequestDTO dto);
}