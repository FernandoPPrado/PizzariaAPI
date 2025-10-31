package com.pizzaria.demo.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        logger.error("Falha de autenticação: {}", authException.getMessage());

        String message = authException.getMessage() != null ? authException.getMessage() : "Unauthorized";
        int status = HttpServletResponse.SC_UNAUTHORIZED; // default
        String customMessage = "Unauthorized";

        if (authException instanceof BadCredentialsException) {
            if (message.contains("expirado")) {
                status = HttpServletResponse.SC_UNAUTHORIZED;
                customMessage = "Token JWT expirado";
            } else if (message.contains("malformado")) {
                status = HttpServletResponse.SC_BAD_REQUEST;
                customMessage = "Token JWT malformado";
            } else if (message.contains("Assinatura inválida")) {
                status = HttpServletResponse.SC_UNAUTHORIZED;
                customMessage = "Assinatura do token inválida";
            } else if (message.contains("não suportado")) {
                status = HttpServletResponse.SC_BAD_REQUEST;
                customMessage = "Token JWT não suportado";
            } else if (message.contains("vazio")) {
                status = HttpServletResponse.SC_BAD_REQUEST;
                customMessage = "Token JWT vazio ou ausente";
            } else {
                status = HttpServletResponse.SC_UNAUTHORIZED;
                customMessage = "Credenciais inválidas";
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        String jsonResponse = String.format("{\"error\": \"%s\", \"status\": %d}", customMessage, status);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}