package com.beautyManager.beautyManagerApi.service.businessService;

import com.beautyManager.beautyManagerApi.dto.configDto.BusinessRequestDTO;
import com.beautyManager.beautyManagerApi.dto.configDto.BusinessResponseDTO;

public interface BusinessService {

    BusinessResponseDTO get();

    BusinessResponseDTO update(BusinessRequestDTO dto);
}