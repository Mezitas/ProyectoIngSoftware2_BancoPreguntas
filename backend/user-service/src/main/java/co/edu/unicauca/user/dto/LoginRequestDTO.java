package co.edu.unicauca.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "el usuario es obligatorio") String username,
        @NotBlank(message = "la contraseña es obligatoria") String password) {
}
