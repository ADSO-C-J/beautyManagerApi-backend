package com.beautyManager.beautyManagerApi.service.staffServiceService;

import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffServiceDto.StaffServiceAssignRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffServiceDto.StaffServiceResponseDTO;

import java.util.List;
import java.util.UUID;

public interface StaffServiceService {

    List<StaffServiceResponseDTO> findByStaff(UUID staffId);

    List<StaffServiceResponseDTO> findByService(UUID serviceId);

    List<ServiceResponseDTO> findAvailableByStaff(UUID staffId);

    List<StaffServiceResponseDTO> assign(UUID staffId, StaffServiceAssignRequestDTO dto);

    void unassign(UUID staffId, UUID serviceId);
}