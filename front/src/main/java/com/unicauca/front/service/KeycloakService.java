package com.unicauca.front.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class KeycloakService {

    private final RestTemplate restTemplate;
    private final String KEYCLOAK_URL = "http://localhost:9090";
    private final String REALM = "trabajos-grado";
    private final String CLIENT_ID = "api-gateway";
    private final String CLIENT_SECRET = "lIvpYlVrTtMFlSmh7uPJdfsMqip8bTyr";
    private final ObjectMapper objectMapper = new ObjectMapper();

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
     * Obtener información completa del usuario incluyendo roles
     */
    public Map<String, Object> getUserInfoWithRoles(String token) {
        try {
            // Decodificar el token JWT para obtener roles
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            
            // Decodificar el payload del JWT
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> tokenData = objectMapper.readValue(payload, Map.class);
            
            // Extraer información básica
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("email", tokenData.get("email"));
            userInfo.put("given_name", tokenData.get("given_name"));
            userInfo.put("family_name", tokenData.get("family_name"));
            userInfo.put("preferred_username", tokenData.get("preferred_username"));
            userInfo.put("sub", tokenData.get("sub")); // ID del usuario
            
            // Extraer roles de Keycloak
            if (tokenData.containsKey("realm_access")) {
                Map<String, Object> realmAccess = (Map<String, Object>) tokenData.get("realm_access");
                if (realmAccess.containsKey("roles")) {
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    userInfo.put("roles", roles);
                    
                    // Asignar el primer rol como rol principal
                    if (!roles.isEmpty()) {
                        userInfo.put("mainRole", roles.get(0).toUpperCase());
                    }
                }
            }
            
            // También verificar roles de client específico
            if (tokenData.containsKey("resource_access")) {
                Map<String, Object> resourceAccess = (Map<String, Object>) tokenData.get("resource_access");
                if (resourceAccess.containsKey(CLIENT_ID)) {
                    Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(CLIENT_ID);
                    if (clientAccess.containsKey("roles")) {
                        List<String> clientRoles = (List<String>) clientAccess.get("roles");
                        userInfo.put("client_roles", clientRoles);
                    }
                }
            }
            
            return userInfo;
            
        } catch (Exception e) {
            System.out.println("Error decodificando token: " + e.getMessage());
            // Fallback: usar el endpoint userinfo
            return getUserInfo(token);
        }
    }

    /**
     * Obtener información del usuario desde el endpoint userinfo
     */
    public Map<String, Object> getUserInfo(String token) {
        String url = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/userinfo";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> userInfo = response.getBody();
            
            // Agregar rol por defecto si no existe
            if (userInfo != null && !userInfo.containsKey("mainRole")) {
                userInfo.put("mainRole", "USER");
            }
            
            return userInfo;
        } catch (Exception e) {
            System.out.println("Error obteniendo user info: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registrar usuario en Keycloak (con mejor logging)
     */
    public boolean registerUser(String username, String password, String firstName, String lastName, String role) {
        try {
            System.out.println("=== REGISTERING USER IN KEYCLOAK ===");
            System.out.println("Username: " + username);
            System.out.println("Role: " + role);
            System.out.println("Name: " + firstName + " " + lastName);
            
            // 1. Primero obtener token de administrador
            System.out.println("Step 1: Obtaining admin token...");
            String adminToken = getAdminToken();
            if (adminToken == null) {
                System.out.println("❌ No se pudo obtener token de administrador");
                System.out.println("⚠️ Verifica que el usuario admin exista en Keycloak");
                return false;
            }
            
            System.out.println("✅ Admin token obtained, length: " + adminToken.length());

            // 2. Crear usuario en Keycloak
            System.out.println("Step 2: Creating user in Keycloak...");
            String createUserUrl = KEYCLOAK_URL + "/admin/realms/" + REALM + "/users";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);
            
            // Construir el objeto de usuario para Keycloak
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("username", username);
            userRequest.put("email", username);
            userRequest.put("firstName", firstName);
            userRequest.put("lastName", lastName);
            userRequest.put("enabled", true);
            userRequest.put("emailVerified", true);
            
            // Credenciales
            List<Map<String, Object>> credentials = new ArrayList<>();
            Map<String, Object> credential = new HashMap<>();
            credential.put("type", "password");
            credential.put("value", password);
            credential.put("temporary", false);
            credentials.add(credential);
            userRequest.put("credentials", credentials);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userRequest, headers);
            
            // Crear usuario
            ResponseEntity<String> createResponse = restTemplate.exchange(createUserUrl, HttpMethod.POST, entity, String.class);
            
            System.out.println("Create user response: " + createResponse.getStatusCode());
            
            if (!createResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("❌ Error creando usuario en Keycloak: " + createResponse.getStatusCode());
                if (createResponse.getStatusCode() == HttpStatus.CONFLICT) {
                    System.out.println("⚠️ El usuario ya existe en Keycloak");
                }
                return false;
            }
            
            System.out.println("✅ Usuario creado en Keycloak");
            
            // 3. Obtener ID del usuario recién creado
            System.out.println("Step 3: Getting user ID...");
            String userId = getUserIdByUsername(adminToken, username);
            if (userId == null) {
                System.out.println("❌ No se pudo obtener ID del usuario creado");
                return false;
            }
            
            System.out.println("✅ User ID obtenido: " + userId);
            
            // 4. Asignar rol al usuario
            System.out.println("Step 4: Assigning role '" + role + "' to user...");
            boolean roleAssigned = assignRoleToUser(adminToken, userId, role);
            if (!roleAssigned) {
                System.out.println("❌ No se pudo asignar rol al usuario");
                System.out.println("⚠️ Verifica que el rol '" + role + "' exista en Keycloak");
                return false;
            }
            
            System.out.println("✅ Rol asignado correctamente");
            System.out.println("🎉 Usuario registrado exitosamente en Keycloak");
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ Error registrando usuario en Keycloak: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener ID de usuario por username
     */
    private String getUserIdByUsername(String adminToken, String username) {
        try {
            String searchUrl = KEYCLOAK_URL + "/admin/realms/" + REALM + "/users?username=" + username;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List> response = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isEmpty()) {
                List<Map<String, Object>> users = response.getBody();
                if (!users.isEmpty()) {
                    return (String) users.get(0).get("id");
                }
            }
        } catch (Exception e) {
            System.out.println("Error obteniendo user ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Asignar rol a usuario
     */
    private boolean assignRoleToUser(String adminToken, String userId, String roleName) {
        try {
            // Primero obtener el rol
            String getRoleUrl = KEYCLOAK_URL + "/admin/realms/" + REALM + "/roles/" + roleName;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> roleResponse = restTemplate.exchange(getRoleUrl, HttpMethod.GET, entity, Map.class);
            
            if (!roleResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("❌ Rol no encontrado: " + roleName);
                return false;
            }
            
            Map<String, Object> role = roleResponse.getBody();
            
            // Asignar rol al usuario
            String assignRoleUrl = KEYCLOAK_URL + "/admin/realms/" + REALM + "/users/" + userId + "/role-mappings/realm";
            
            List<Map<String, Object>> rolesToAssign = new ArrayList<>();
            Map<String, Object> roleMapping = new HashMap<>();
            roleMapping.put("id", role.get("id"));
            roleMapping.put("name", role.get("name"));
            rolesToAssign.add(roleMapping);
            
            HttpEntity<List<Map<String, Object>>> assignEntity = new HttpEntity<>(rolesToAssign, headers);
            
            ResponseEntity<String> assignResponse = restTemplate.exchange(assignRoleUrl, HttpMethod.POST, assignEntity, String.class);
            
            return assignResponse.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            System.out.println("Error asignando rol: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtener token de administrador mejorado
     */
    public String getAdminToken() {
        try {
            System.out.println("=== OBTAINING ADMIN TOKEN ===");
            
            // Usar las credenciales específicas del admin
            String adminUsername = "keycloak-admin";
            String adminPassword = "admin";
            
            String token = login(adminUsername, adminPassword);
            
            if (token != null) {
                System.out.println("✅ Admin token obtained successfully");
                return token;
            } else {
                System.out.println("❌ Failed to obtain admin token");
                return null;
            }
            
        } catch (Exception e) {
            System.out.println("Error obteniendo admin token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método de diagnóstico para verificar conexión con Keycloak
     */
    public void testConnection() {
        try {
            System.out.println("=== KEYCLOAK CONNECTION DIAGNOSTIC ===");
            System.out.println("Keycloak URL: " + KEYCLOAK_URL);
            System.out.println("Realm: " + REALM);
            System.out.println("Client ID: " + CLIENT_ID);
            
            // Probar si el realm existe
            String realmUrl = KEYCLOAK_URL + "/realms/" + REALM;
            try {
                ResponseEntity<String> realmResponse = restTemplate.getForEntity(realmUrl, String.class);
                System.out.println("✅ Realm accessible: " + realmResponse.getStatusCode());
            } catch (Exception e) {
                System.out.println("❌ Realm NOT accessible: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Diagnostic failed: " + e.getMessage());
        }
    }
}