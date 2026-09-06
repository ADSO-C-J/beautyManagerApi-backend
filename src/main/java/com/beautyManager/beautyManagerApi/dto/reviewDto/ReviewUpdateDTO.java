package com.beautyManager.beautyManagerApi.dto.reviewDto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewUpdateDTO {

    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Short rating;

    @Size(max = 1000, message = "El comentario no debe superar los 1000 caracteres")
    private String comment;

    private Boolean isPublic;
}