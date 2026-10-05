package com.beautyManager.beautyManagerApi.dto.clientDto;

import java.util.UUID;
import lombok.Data;

@Data
public class ClientResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
}