package com.pizzaria.demo.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Validação do token
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUserNameFromJwtToken(token);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Autenticação definida para usuário: {}", username);
                }

            } catch (ExpiredJwtException e) {
                logger.warn("Token expirado: {}", e.getMessage());
                request.setAttribute("jwt_error", "Token expirado");
                throw new BadCredentialsException("Token expirado", e);

            } catch (MalformedJwtException e) {
                logger.warn("Token malformado: {}", e.getMessage());
                request.setAttribute("jwt_error", "Token malformado");
                throw new BadCredentialsException("Token malformado", e);

            } catch (SignatureException e) {
                logger.warn("Assinatura inválida: {}", e.getMessage());
                request.setAttribute("jwt_error", "Assinatura inválida");
                throw new BadCredentialsException("Assinatura inválida", e);

            } catch (UnsupportedJwtException e) {
                logger.warn("Token não suportado: {}", e.getMessage());
                request.setAttribute("jwt_error", "Token não suportado");
                throw new BadCredentialsException("Token não suportado", e);

            } catch (IllegalArgumentException e) {
                logger.warn("Token vazio ou inválido: {}", e.getMessage());
                request.setAttribute("jwt_error", "Token vazio ou inválido");
                throw new BadCredentialsException("Token vazio ou inválido", e);

            } catch (AuthenticationException e) {
                logger.warn("Erro de autenticação: {}", e.getMessage());
                request.setAttribute("jwt_error", "Erro de autenticação");
                throw e;

            } catch (JwtException e) {
                logger.error("Erro genérico de JWT: {}", e.getMessage());
                request.setAttribute("jwt_error", "Erro genérico de token");
                throw new BadCredentialsException("Erro genérico de token", e);

            } catch (Exception e) {
                logger.error("Erro inesperado no filtro JWT: {}", e.getMessage());
                request.setAttribute("jwt_error", "Erro inesperado no processamento do token");
                throw new ServletException("Erro inesperado no processamento do token", e);
            }

        } else {
            logger.debug("Nenhum header Authorization encontrado");
            // Sem token: SecurityContext fica vazio, EntryPoint será chamado se endpoint protegido
        }

        filterChain.doFilter(request, response);
    }
}