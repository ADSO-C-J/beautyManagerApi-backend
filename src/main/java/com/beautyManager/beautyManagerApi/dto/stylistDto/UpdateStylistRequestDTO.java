package com.beautyManager.beautyManagerApi.dto.stylistDto;

import java.util.UUID;
import lombok.Data;

@Data
public class UpdateStylistRequestDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;

}
