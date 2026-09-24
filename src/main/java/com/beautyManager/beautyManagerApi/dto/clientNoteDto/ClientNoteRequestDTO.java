package com.beautyManager.beautyManagerApi.dto.clientNoteDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientNoteRequestDTO {

    @NotBlank(message = "El contenido de la nota es obligatorio")
    @Size(max = 5000, message = "La nota no debe exceder 5000 caracteres")
    private String content;
}