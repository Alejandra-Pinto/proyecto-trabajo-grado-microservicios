package com.example.users.controller;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.users.entity.User;
import com.example.users.infra.dto.UserRequest;
import com.example.users.service.IUserService;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final IUserService service;

    public UserController(IUserService service) {
        this.service = service;
    }

    @PostMapping("/sync-user")
    public ResponseEntity<User> syncUserWithKeycloak(@RequestBody UserRequest userRequest) {
        try {
            System.out.println("=== SYNC USER FROM KEYCLOAK ===");
            System.out.println("Email: " + userRequest.getEmail());
            System.out.println("Role: " + userRequest.getRole());
            System.out.println("Status: " + userRequest.getStatus());

            // Verificar si el usuario ya existe
            Optional<User> existingUser = service.findByEmail(userRequest.getEmail());
            if (existingUser.isPresent()) {
                System.out.println("✅ Usuario ya existe, retornando existente");
                return ResponseEntity.ok(existingUser.get());
            }

            // Crear UserRequest para el servicio existente
            UserRequest request = new UserRequest();
            request.setFirstName(userRequest.getFirstName());
            request.setLastName(userRequest.getLastName());
            request.setProgram(userRequest.getProgram());
            request.setEmail(userRequest.getEmail());
            request.setPassword("temp-password-" + System.currentTimeMillis()); // Password temporal
            request.setRole(userRequest.getRole());
            request.setStatus(userRequest.getStatus());
            request.setPhone("0000000"); // Opcional

            // Usar el método register existente pero sin validaciones de password/email estrictas
            User createdUser = service.syncUserFromKeycloak(request);
            
            System.out.println("✅ Usuario sincronizado exitosamente: " + createdUser.getEmail());
            return ResponseEntity.ok(createdUser);

        } catch (Exception e) {
            System.out.println("❌ Error sincronizando usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String email) {
        String message = service.logout(email);
        return ResponseEntity.ok(message);
    }

    @GetMapping
    public List<User> getAll() {
        return service.getAllUsers();
    }

@GetMapping(value = "/email/{email}", produces = "application/json;charset=UTF-8")
public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
    Optional<User> user = service.findByEmail(email);
    
    if (user.isPresent()) {
        User userEntity = user.get();
        
        // DEBUG: Crear un JSON manualmente para ver la codificación
        String manualJson = String.format(
            "{\"program\":\"%s\", \"email\":\"%s\", \"role\":\"%s\"}", 
            userEntity.getProgram(), userEntity.getEmail(), userEntity.getRole()
        );
        
        System.out.println("=== MANUAL JSON DEBUG ===");
        System.out.println("Manual JSON: " + manualJson);
        System.out.println("Program bytes: " + Arrays.toString(userEntity.getProgram().getBytes(StandardCharsets.UTF_8)));
        System.out.println("=== END DEBUG ===");
    }
    
    return user.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}

    @GetMapping("/rol/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        try {
            List<User> users = service.findByRole(role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
