package com.example.users.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/usuarios/sync-user").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )

            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            )

            // IMPORTANTE: esto dice a Spring Security:
            // "si no viene un JWT, no bloquees la petición"
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            
            );

        return http.build();
    }
}
