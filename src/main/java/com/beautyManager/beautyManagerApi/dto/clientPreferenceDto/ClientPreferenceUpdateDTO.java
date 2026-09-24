package com.beautyManager.beautyManagerApi.dto.clientPreferenceDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientPreferenceUpdateDTO {

    @NotBlank(message = "El valor de la preferencia es obligatorio")
    @Size(max = 5000, message = "El valor no debe exceder 5000 caracteres")
    private String value;
}