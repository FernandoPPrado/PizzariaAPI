package com.pizzaria.demo.user.controller;

import com.pizzaria.demo.product.dto.ProductRequestDTO;
import com.pizzaria.demo.product.dto.ProductResponseDTO;
import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.user.dto.UserRequestDTO;
import com.pizzaria.demo.user.dto.UserResponseDTO;
import com.pizzaria.demo.user.model.Role;
import com.pizzaria.demo.user.model.User;
import com.pizzaria.demo.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping(path = "/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findById(id));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping(path = "/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer id, @RequestBody @Valid UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userRequestDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
