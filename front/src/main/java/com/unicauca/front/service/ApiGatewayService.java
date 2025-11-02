package com.unicauca.front.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiGatewayService {
    private final RestTemplate restTemplate;
    private final String API_GATEWAY_URL = "http://localhost:8080";

    public ApiGatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //Método genérico para POST (login, crear, etc.)
    public <T> ResponseEntity<T> post(String microservicio, String endpoint, Object request, Class<T> responseType) {
        String url = API_GATEWAY_URL + "/" + microservicio + endpoint;
        // AGREGAR ESTO TEMPORALMENTE PARA DEBUG
        System.out.println("=== DEBUG API GATEWAY ===");
        System.out.println("API_GATEWAY_URL: " + API_GATEWAY_URL);
        System.out.println("microservicio: " + microservicio);
        System.out.println("endpoint: " + endpoint);
        System.out.println("URL FINAL: " + url);
        System.out.println("=== FIN DEBUG ===");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
    }

    //Método genérico para GET (obtener datos)
    public <T> ResponseEntity<T> get(String microservicio, String endpoint, Class<T> responseType) {
        String url = API_GATEWAY_URL + "/" + microservicio + endpoint;
        return restTemplate.getForEntity(url, responseType);
    }

    //Método genérico para PUT (actualizar)
    public <T> ResponseEntity<T> put(String microservicio, String endpoint, Object request, Class<T> responseType) {
        String url = API_GATEWAY_URL + "/" + microservicio + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
    }

    //Método genérico para DELETE
    public ResponseEntity<Void> delete(String microservicio, String endpoint) {
        String url = API_GATEWAY_URL + "/" + microservicio + endpoint;
        return restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
    }
}