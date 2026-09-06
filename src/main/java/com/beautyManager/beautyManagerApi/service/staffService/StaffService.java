package com.beautyManager.beautyManagerApi.service.staffService;

import com.beautyManager.beautyManagerApi.dto.staffDto.StaffRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffDto.StaffResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffDto.StaffUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface StaffService {

    List<StaffResponseDTO> findAll();

    StaffResponseDTO findById(UUID id);

    List<StaffResponseDTO> findByBusiness(UUID businessId);

    StaffResponseDTO create(StaffRequestDTO dto);

    StaffResponseDTO update(UUID id, StaffUpdateDTO dto);

    void delete(UUID id);
}
