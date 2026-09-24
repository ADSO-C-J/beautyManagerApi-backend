package com.beautyManager.beautyManagerApi.dto.clientNoteDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ClientNoteResponseDTO {

    private UUID id;
    private UUID clientId;
    private String clientName;
    private UUID staffId;
    private String staffName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}