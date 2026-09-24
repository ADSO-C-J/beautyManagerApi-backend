package com.beautyManager.beautyManagerApi.service.clientNoteService;

import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteRequestDTO;
import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteResponseDTO;
import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface ClientNoteService {

    List<ClientNoteResponseDTO> findByClient(UUID clientId);

    ClientNoteResponseDTO findById(UUID id);

    ClientNoteResponseDTO create(UUID clientId, ClientNoteRequestDTO dto);

    ClientNoteResponseDTO update(UUID id, ClientNoteUpdateDTO dto);

    void delete(UUID id);
}