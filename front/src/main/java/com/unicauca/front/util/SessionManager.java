package com.unicauca.front.util;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {
    private static User currentUser;
    private static String accessToken;
    private static ApiGatewayService apiGatewayService;

    // Inyectar el servicio via setter
    public static void setApiGatewayService(ApiGatewayService service) {
        apiGatewayService = service;
    }

    public static void setCurrentUser(User user) {
        System.out.println("=== SESSION MANAGER ===");
        System.out.println("Setting current user: " + user);
        System.out.println("User program: " + (user != null ? user.getProgram() : "null"));
        
        // DEBUG: Mostrar de dónde viene la llamada
        Thread.dumpStack();
        
        System.out.println("=== END SESSION MANAGER ===");
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setAccessToken(String token) {
        accessToken = token;
        // Actualizar también en ApiGatewayService
        if (apiGatewayService != null) {
            apiGatewayService.setAccessToken(token);
        }
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void clearSession() {
        currentUser = null;
        accessToken = null;
        if (apiGatewayService != null) {
            apiGatewayService.setAccessToken(null);
        }
    }

    public static boolean isLoggedIn() {
        return currentUser != null && accessToken != null;
    }
}