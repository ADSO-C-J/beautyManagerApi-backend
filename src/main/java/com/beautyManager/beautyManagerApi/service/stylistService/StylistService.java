package com.beautyManager.beautyManagerApi.service.stylistService;

import com.beautyManager.beautyManagerApi.dto.StylistResponseDTO;

import java.util.List;

public interface StylistService {
    List<StylistResponseDTO> findAll();
}