package com.beautyManager.beautyManagerApi.dto.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessRequestDTO {

    @NotBlank(message = "El nombre del negocio es obligatorio")
    private String name;

    private String address;
    private String city;

    @NotBlank(message = "El país es obligatorio")
    private String country;

    private String phone;

    @Email(message = "Formato de email inválido")
    private String email;

    private String website;
    private String logoUrl;

    @NotBlank(message = "La moneda es obligatoria")
    private String currency;

    private String timezone;
}