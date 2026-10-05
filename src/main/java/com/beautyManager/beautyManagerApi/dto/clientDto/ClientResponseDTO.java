package com.beautyManager.beautyManagerApi.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class ClientResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
}