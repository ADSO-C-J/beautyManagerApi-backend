package com.beautyManager.beautyManagerApi.dto.userDto;

import com.beautyManager.beautyManagerApi.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;

    private String phone;

    @NotNull(message = "El rol es obligatorio")
    private UserRole role;
}
