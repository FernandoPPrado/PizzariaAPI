package com.pizzaria.demo.user.dto;

import jakarta.validation.constraints.*;


public record UserRequestDTO(
        @NotBlank
        @Size(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres")
        String name,
        @Email
        @NotBlank
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email,
        @NotBlank
        @Size(min = 6, max = 20, message = "A senha deve ter entre 6 e 20 caracteres")
        String password
) {
}
