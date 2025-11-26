package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
            .cors(cors -> {}) // usa el CorsWebFilter que ya tienes
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges

                // Públicos
                .pathMatchers("/api/usuarios/login", "/api/usuarios/register").permitAll()

                // Admin
                .pathMatchers("/api/admin/**").hasRole("ADMIN")

                // Docente
                .pathMatchers("/api/evaluaciones/**").hasRole("PROFESSOR")

                // Degreeworks: Docentes y Estudiantes
                .pathMatchers("/api/degreeworks/**").hasAnyRole("PROFESSOR", "STUDENT")


                // Todo lo demás requiere token
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);

        return http.build();
    }
}
