package com.unicauca.front.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiGatewayService {
    private final RestTemplate restTemplate;
    private final String API_GATEWAY_URL = "http://localhost:8080";
    private String accessToken; // Nuevo: almacenar el token

    public ApiGatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Nuevo: Método para guardar el token
    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    // Nuevo: Método para obtener el token
    public String getAccessToken() {
        return accessToken;
    }

    // Método genérico para POST (login, crear, etc.)
    public <T> ResponseEntity<T> post(String microservicio, String endpoint, Object request, Class<T> responseType) {
        return post(microservicio, endpoint, request, responseType, true);
    }

    // Nuevo: Método POST sobrecargado para controlar si incluye token
    public <T> ResponseEntity<T> post(String microservicio, String endpoint, Object request, Class<T> responseType, boolean includeToken) {
        String url = API_GATEWAY_URL + "/" + microservicio + endpoint;
        
        System.out.println("=== DEBUG API GATEWAY ===");
        System.out.println("URL: " + url);
        System.out.println("Incluir token: " + includeToken);
        System.out.println("=== FIN DEBUG ===");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Nuevo: Agregar token de autorización si está disponible y se requiere
        if (includeToken && accessToken != null && !accessToken.isEmpty()) {
            headers.setBearerAuth(accessToken);
            System.out.println("✅ Token incluido en la request");
        }
        
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
    }

    // Método genérico para GET (obtener datos)
    public <T> ResponseEntity<T> get(String microservicio, String endpoint, Class<T> responseType) {
        String url = API_GATEWAY_URL + "/" + microservicio + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        
        // Nuevo: Agregar token de autorización
        if (accessToken != null && !accessToken.isEmpty()) {
            headers.setBearerAuth(accessToken);
            System.out.println("✅ Token incluido en GET request");
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
    }

    // Métodos PUT y DELETE similares...
    public <T> ResponseEntity<T> put(String microservicio, String endpoint, Object request, Class<T> responseType) {
        String url = API_GATEWAY_URL + "/" + microservicio + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (accessToken != null && !accessToken.isEmpty()) {
            headers.setBearerAuth(accessToken);
        }
        
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
    }

    public ResponseEntity<Void> delete(String microservicio, String endpoint) {
        String url = API_GATEWAY_URL + "/" + microservicio + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        
        if (accessToken != null && !accessToken.isEmpty()) {
            headers.setBearerAuth(accessToken);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}