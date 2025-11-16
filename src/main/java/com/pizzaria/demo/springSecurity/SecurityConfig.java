package com.pizzaria.demo.springSecurity;

import com.pizzaria.demo.jwt.AuthTokenFilter;
import com.pizzaria.demo.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // habilita @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

    private final UserService userService;
    private final AuthTokenFilter authTokenFilter;
    private final PasswordEncoder passwordEncoder; // vem do PasswordConfig

    // rotas públicas (sem autenticação)
    private static final String[] PUBLIC_MATCHERS = {
            "/auth/**",
            "/mercado-pago/webhook" // webhook do Mercado Pago precisa ser público
    };

    public SecurityConfig(UserService userService,
                          AuthTokenFilter authTokenFilter,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authTokenFilter = authTokenFilter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API REST + JWT -> sem CSRF, sem sessão
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_MATCHERS).permitAll() // libera rotas públicas
                        .anyRequest().authenticated()                 // resto precisa de JWT
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint()) // trata 401
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)

                // desabilita mecanismos de login padrão do Spring Security
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService); // usa seu UserService
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // EntryPoint para retornar 401 em vez de redirecionar
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Erro: Não autorizado");
    }
}
