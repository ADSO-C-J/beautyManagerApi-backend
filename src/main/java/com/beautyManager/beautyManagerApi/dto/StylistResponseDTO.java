package com.beautyManager.beautyManagerApi.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class StylistResponseDTO {
    private UUID id;
    private String name;
    private String specialty;
    private String avatarUrl;
}