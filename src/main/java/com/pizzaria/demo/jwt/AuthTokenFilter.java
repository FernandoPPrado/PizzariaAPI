package com.pizzaria.demo.jwt;

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
                // Validação agora lança JwtException se inválido
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUserNameFromJwtToken(token); // Pode lançar JwtException

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Pode lançar UsernameNotFoundException (sub de AuthenticationException)

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
            } catch (JwtException e) {
                // Wrapping específico: transforma JwtException em exceções Spring Security para granularidade
                logger.debug("Token JWT inválido: {}", e.getMessage());
                if (e.getMessage().contains("expired")) {
                    throw new BadCredentialsException("Token expirado", e); // 401 via EntryPoint
                } else {
                    throw new BadCredentialsException("Token inválido ou malformado", e); // 400/401 via EntryPoint
                }
            } catch (AuthenticationException e) {
                // Propaga exceções do UserDetailsService (ex.: usuário não encontrado)
                logger.debug("Erro de autenticação: {}", e.getMessage());
                throw e; // Vai para EntryPoint
            } catch (Exception e) {
                // Para erros inesperados, loga e propaga (pode cair em 500 via handler global)
                logger.error("Erro inesperado no filtro JWT: {}", e.getMessage());
                throw new ServletException("Erro no processamento do token", e);
            }
        } else {
            logger.debug("Nenhum header Authorization encontrado");
            // Sem token: SecurityContext fica vazio, EntryPoint será chamado se endpoint protegido
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}