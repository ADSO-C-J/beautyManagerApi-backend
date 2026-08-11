package com.beautyManager.beautyManagerApi.service.clientService;

import com.beautyManager.beautyManagerApi.dto.ClientResponseDTO;

import java.util.List;

public interface ClientService {
    List<ClientResponseDTO> search(String query);
}