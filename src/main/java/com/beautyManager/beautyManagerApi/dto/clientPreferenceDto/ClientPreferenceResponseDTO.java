package com.beautyManager.beautyManagerApi.dto.clientPreferenceDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ClientPreferenceResponseDTO {

    private UUID id;
    private UUID clientId;
    private String clientName;
    private String key;
    private String value;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}