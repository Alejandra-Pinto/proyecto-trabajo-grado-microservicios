package com.example.users.controller;

import com.example.users.entity.Admin;
import com.example.users.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<Admin> register(@RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.register(admin));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        return adminService.login(email, password)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).body("Credenciales inválidas"));
    }

    @PutMapping("/approve/{email}")
    public ResponseEntity<String> approveUser(@PathVariable String email) {
        return ResponseEntity.ok(adminService.approveUser(email));
    }

    @PutMapping("/reject/{email}")
    public ResponseEntity<String> rejectUser(@PathVariable String email) {
        return ResponseEntity.ok(adminService.rejectUser(email));
    }
}
