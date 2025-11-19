package com.pizzaria.demo.jwt.controller;

import com.pizzaria.demo.jwt.JwtUtils;
import com.pizzaria.demo.jwt.dto.AuthLoginDto;
import com.pizzaria.demo.user.dto.UserRequestDTO;
import com.pizzaria.demo.user.dto.UserResponseDTO;
import com.pizzaria.demo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid AuthLoginDto request) {

        log.info("Recebida requisição de login para email={}", request.email());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        log.debug("Autenticação bem sucedida para email={}", request.email());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);

        log.info("Token JWT gerado com sucesso para email={}", request.email());

        return ResponseEntity.ok(jwt);
    }


    @PostMapping(path = "/create")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        log.info("Recebida requisição para criação de usuário email={}", userRequestDTO.email());
        UserResponseDTO user = userService.createUser(userRequestDTO);
        log.info("Usuário criado com sucesso na requisição id={} email={}", user.id(), user.email());
        return ResponseEntity.ok(user);
    }


}
