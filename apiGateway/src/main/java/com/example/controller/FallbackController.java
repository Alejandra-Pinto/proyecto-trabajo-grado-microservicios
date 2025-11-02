package com.example.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/proyectos")
    public Mono<ResponseEntity<String>> proyectoFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("El servicio de proyectos no está disponible temporalmente. Inténtalo más tarde."));
    }

    @GetMapping("/usuarios")
    public Mono<ResponseEntity<String>> usuarioFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("El servicio de usuarios no está disponible temporalmente. Inténtalo más tarde."));
    }

    @GetMapping("/evaluaciones")
    public Mono<ResponseEntity<String>> evaluacionFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("El servicio de evaluaciones no está disponible temporalmente. Inténtalo más tarde."));
    }

    @GetMapping("/reportes")
    public Mono<ResponseEntity<String>> reporteFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("El servicio de reportes no está disponible temporalmente. Inténtalo más tarde."));
    }

    @GetMapping("/notificaciones")
    public Mono<ResponseEntity<String>> notificacionFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("El servicio de notificaciones no está disponible temporalmente. Inténtalo más tarde."));
    }
}

