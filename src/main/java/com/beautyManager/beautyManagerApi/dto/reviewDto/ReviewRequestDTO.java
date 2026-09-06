package com.beautyManager.beautyManagerApi.dto.reviewDto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewRequestDTO {

    @NotNull(message = "El appointment ID es obligatorio")
    private UUID appointmentId;

    @NotNull(message = "El client ID es obligatorio")
    private UUID clientId;

    private UUID staffId;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Short rating;

    @Size(max = 1000, message = "El comentario no debe superar los 1000 caracteres")
    private String comment;

    @NotNull(message = "El campo isPublic es obligatorio")
    private Boolean isPublic;
}