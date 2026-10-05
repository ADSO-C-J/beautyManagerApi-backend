package com.beautyManager.beautyManagerApi.dto.stylistDto;
import lombok.Data;

@Data
public class CreateStylistRequestDTO {

    private String name;
    private String email;
    private String phone;

}
