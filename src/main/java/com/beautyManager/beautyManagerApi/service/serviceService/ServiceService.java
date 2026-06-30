package com.beautyManager.beautyManagerApi.service.serviceService;
import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceRequestDTO;
import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ServiceService {
    List<ServiceResponseDTO> findAll();
    ServiceResponseDTO findById(UUID id);
    ServiceResponseDTO create(ServiceRequestDTO dto);
    ServiceResponseDTO update(UUID id, ServiceRequestDTO dto);
    void delete(UUID id);
}
