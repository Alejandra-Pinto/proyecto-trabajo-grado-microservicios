package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.HttpMethod;


@Configuration
@EnableWebFluxSecurity 
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        System.out.println("=== SECURITY CONFIG LOADED ===");
        
        http
            .cors(cors -> {})
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> {
                System.out.println("Configuring authorization rules...");
                exchanges
                    .pathMatchers("/api/usuarios/login", "/api/usuarios/register", "/api/usuarios/sync-user").permitAll()
                    .pathMatchers("/api/admin/**").hasRole("ADMIN")
                    .pathMatchers("/api/evaluaciones/**").hasRole("PROFESSOR")
                    .pathMatchers("/api/degreeworks/**").hasAnyRole("PROFESSOR", "STUDENT")
                    .anyExchange().authenticated();
            })
            .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);

        return http.build();
    }
}
