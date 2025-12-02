package com.example.config;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        System.out.println("!!! SECURITY CONFIG LOADED - CONFIGURING RULES !!!");
        
        // Convertidor JWT para Keycloak
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");
        
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Obtener autoridades del convertidor por defecto
            var authorities = authoritiesConverter.convert(jwt);
            
            // También extraer roles de realm_access (Keycloak)
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                if (realmRoles != null) {
                    for (String role : realmRoles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }
            
            return authorities;
        });
        
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.disable()) // Temporal - ya tienes CorsConfig
            .authorizeExchange(exchanges -> {
                System.out.println("!!! SETTING UP AUTHORIZATION RULES !!!");
                
                exchanges
                    // Endpoints públicos
                    .pathMatchers(
                        "/api/usuarios/login",
                        "/api/usuarios/register", 
                        "/api/usuarios/sync-user",
                        "/api/degreeworks/**"
                    ).permitAll()
                    
                    // Admin routes
                    .pathMatchers("/api/admin/**").hasRole("ADMIN")
                    
                    // Evaluaciones - solo PROFESSOR (agrega COORDINATOR si es necesario)
                    .pathMatchers("/api/evaluaciones/**").hasAnyRole("COORDINATOR", "DEPARTMENT_HEAD")
                    
                    
                    
                    // Todo lo demás requiere autenticación
                    .anyExchange().authenticated();
            })
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(
                        new ReactiveJwtAuthenticationConverterAdapter(jwtConverter)
                    )
                )
            )
            .build();
    }
}