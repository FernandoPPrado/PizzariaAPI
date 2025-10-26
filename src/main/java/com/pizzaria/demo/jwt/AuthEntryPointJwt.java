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
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        logger.error("Falha de autenticação: {}", authException.getMessage());

        // Análise granular: usa instanceof apenas para subclasses reais de AuthenticationException
        // e análise de mensagem para erros JWT embrulhados (sem casts inválidos)
        String message = authException.getMessage() != null ? authException.getMessage() : "Unauthorized";
        int status = HttpServletResponse.SC_UNAUTHORIZED; // Default 401
        String customMessage = "Unauthorized"; // Mensagem genérica por default

        if (authException instanceof BadCredentialsException || message.contains("Token inválido") || message.contains("malformed") || message.contains("invalid signature")) {
            status = HttpServletResponse.SC_BAD_REQUEST; // 400 para token malformado/inválido
            customMessage = "Token JWT inválido ou malformado";
        } else if (message.contains("expired") || message.contains("Token expirado")) {
            status = HttpServletResponse.SC_UNAUTHORIZED; // 401 para token expirado
            customMessage = "Token JWT expirado";
        } else if (authException.getMessage().contains("Bad credentials")) {
            status = HttpServletResponse.SC_UNAUTHORIZED; // 401 para credenciais inválidas
            customMessage = "Credenciais inválidas";
        }
        // Nota: Para AccessDeniedException (403), configure um AccessDeniedHandler separado na SecurityConfig,
        // ex.: http.exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler());
        // Não trate aqui, pois EntryPoint é só para autenticação (não autorização).

        // Se for um erro interno (ex.: AuthenticationServiceException), pode propagar para handler global (500)
        // ou forçar 401 aqui se preferir.

        // Resposta customizada em JSON (útil para APIs)
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        String jsonResponse = String.format("{\"error\": \"%s\", \"status\": %d}", customMessage, status);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();

        // Não usa response.sendError() para evitar forçar 401; usa setStatus() para flexibilidade
    }
}