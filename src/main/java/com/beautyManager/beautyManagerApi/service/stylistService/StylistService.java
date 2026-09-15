package com.beautyManager.beautyManagerApi.service.stylistService;

import com.beautyManager.beautyManagerApi.dto.CreateStylistRequestDTO;
import com.beautyManager.beautyManagerApi.dto.StylistResponseDTO;
import com.beautyManager.beautyManagerApi.dto.UpdateStylistRequestDTO;



import java.util.List;

import java.util.UUID;

public interface StylistService {
    List<StylistResponseDTO> findAll();

    StylistResponseDTO findById(UUID id);
    StylistResponseDTO create(CreateStylistRequestDTO dto);
    StylistResponseDTO update(UUID id, UpdateStylistRequestDTO dto);
    void delete(UUID id);

}