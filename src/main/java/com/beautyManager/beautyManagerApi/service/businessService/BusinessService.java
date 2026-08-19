package com.beautyManager.beautyManagerApi.service.businessService;

import com.beautyManager.beautyManagerApi.dto.config.BusinessRequestDTO;
import com.beautyManager.beautyManagerApi.dto.config.BusinessResponseDTO;

public interface BusinessService {

    BusinessResponseDTO get();

    BusinessResponseDTO update(BusinessRequestDTO dto);
}