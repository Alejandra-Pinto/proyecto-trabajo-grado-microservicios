package com.example.apiGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// ¡AGREGA ESTO!
@SpringBootApplication
@ComponentScan(basePackages = {"com.example", "com.example.config"})
public class ApiGatewayApplication {
    public static void main(String[] args) {
        System.out.println("=== STARTING API GATEWAY ===");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}