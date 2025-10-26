package com.pizzaria.demo.jwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginDto(
        @Email
        @NotBlank
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email,
        @NotBlank
        @Size(min = 6, max = 20, message = "A senha deve ter entre 6 e 20 caracteres")
        String password

) {
}
