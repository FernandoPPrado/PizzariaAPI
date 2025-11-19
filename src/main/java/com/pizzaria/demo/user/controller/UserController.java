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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
        log.info("Recebida requisição para buscar usuário por id={}", id);
        UserResponseDTO userResp = userService.findById(id);
        log.info("Usuário retornado na requisição id={} email={}", userResp.id(), userResp.email());
        return ResponseEntity.ok(userResp);

    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Recebida requisição para listar todos os usuários ativos");
        List<UserResponseDTO> listUserDto = userService.findAll();
        log.info("Total de usuários retornados na requisição = {}", listUserDto.size());
        return ResponseEntity.ok(listUserDto);

    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping(path = "/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer id, @RequestBody @Valid UserRequestDTO userRequestDTO) {
        log.info("Recebida requisição para atualizar usuário id={}", id);
        UserResponseDTO userResponseDTO = userService.updateUser(id, userRequestDTO);
        log.info("Usuário atualizado na requisição id={} email={}", userResponseDTO.id(), userResponseDTO.email());
        return ResponseEntity.ok(userResponseDTO);
    }


    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {
        log.info("Recebida requisição para desativar (soft delete) usuário id={}", id);
        userService.deleteUserById(id);
        log.info("Usuário desativado com sucesso na requisição id={}", id);
        return ResponseEntity.noContent().build();
    }
}
