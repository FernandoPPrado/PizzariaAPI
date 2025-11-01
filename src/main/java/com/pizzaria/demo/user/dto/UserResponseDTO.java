package com.pizzaria.demo.user.dto;

import com.pizzaria.demo.user.model.Role;
import com.pizzaria.demo.user.model.User;

public record UserResponseDTO(
        Integer id,
        String name,
        String email,
        Role role
) {

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

}
