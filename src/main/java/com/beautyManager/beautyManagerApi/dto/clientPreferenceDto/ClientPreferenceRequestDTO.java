package com.beautyManager.beautyManagerApi.dto.clientPreferenceDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientPreferenceRequestDTO {

    @NotBlank(message = "La clave de la preferencia es obligatoria")
    @Size(max = 100, message = "La clave no debe exceder 100 caracteres")
    private String key;

    @NotBlank(message = "El valor de la preferencia es obligatorio")
    @Size(max = 5000, message = "El valor no debe exceder 5000 caracteres")
    private String value;
}