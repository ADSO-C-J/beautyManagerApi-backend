package com.beautyManager.beautyManagerApi.service.clientPreferenceService;

import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceRequestDTO;
import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceResponseDTO;
import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface ClientPreferenceService {

    List<ClientPreferenceResponseDTO> findByClient(UUID clientId);

    ClientPreferenceResponseDTO findById(UUID id);

    ClientPreferenceResponseDTO create(UUID clientId, ClientPreferenceRequestDTO dto);

    ClientPreferenceResponseDTO update(UUID id, ClientPreferenceUpdateDTO dto);

    void delete(UUID id);
}