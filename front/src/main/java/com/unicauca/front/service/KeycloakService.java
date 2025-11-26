package com.unicauca.front.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KeycloakService {

    private final RestTemplate restTemplate;
    private final String KEYCLOAK_URL = "http://localhost:9090";
    private final String REALM = "trabajos-grado";
    private final String CLIENT_ID = "api-gateway";
    private final String CLIENT_SECRET = "TU_CLIENT_SECRET_AQUI"; // Reemplaza con tu secret

    public KeycloakService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Login con Keycloak
     */
    public String login(String username, String password) {
        String url = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                return (String) responseBody.get("access_token");
            }
        } catch (Exception e) {
            System.out.println("Error en login Keycloak: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Registrar usuario en Keycloak
     */
    public boolean register(String username, String password, String firstName, String lastName, String role) {
        // Primero: Crear usuario en Keycloak (requiere token de admin)
        String createUserUrl = KEYCLOAK_URL + "/admin/realms/" + REALM + "/users";
        
        // Necesitarás un token de administrador para esto
        String adminToken = getAdminToken();
        
        if (adminToken == null) {
            System.out.println("No se pudo obtener token de administrador");
            return false;
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        // Construir el objeto de usuario para Keycloak
        Map<String, Object> user = Map.of(
            "username", username,
            "email", username,
            "firstName", firstName,
            "lastName", lastName,
            "enabled", true,
            "credentials", new Object[]{
                Map.of(
                    "type", "password",
                    "value", password,
                    "temporary", false
                )
            }
        );
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(createUserUrl, HttpMethod.POST, entity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.out.println("Error registrando usuario en Keycloak: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtener información del usuario desde el token
     */
    public Map<String, Object> getUserInfo(String token) {
        String url = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/userinfo";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Error obteniendo user info: " + e.getMessage());
            return null;
        }
    }

    private String getAdminToken() {
        // Implementar obtención de token de administrador
        // Esto es temporal - en producción usar client credentials flow
        return login("admin", "admin"); // Cambia por credenciales de admin reales
    }
}