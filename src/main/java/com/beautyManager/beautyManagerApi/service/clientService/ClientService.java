package com.beautyManager.beautyManagerApi.service.clientService;

import com.beautyManager.beautyManagerApi.dto.ClientResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateClientRequestDTO;
import com.beautyManager.beautyManagerApi.dto.UpdateClientRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ClientService {
    List<ClientResponseDTO> search(String query);
    ClientResponseDTO findById(UUID id);
    ClientResponseDTO create(CreateClientRequestDTO dto);
    ClientResponseDTO update(UUID id, UpdateClientRequestDTO dto);
    ClientResponseDTO partialUpdate(UUID id, UpdateClientRequestDTO dto);
    void delete(UUID id);

}