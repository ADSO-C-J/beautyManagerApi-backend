package com.beautyManager.beautyManagerApi.dto.clientDto;
import lombok.Data;

@Data

public class UpdateClientRequestDTO {
    private String name;
    private String email;
    private String phone;
}
