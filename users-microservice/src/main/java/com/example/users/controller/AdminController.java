package com.example.users.controller;

import com.example.users.entity.Admin;
import com.example.users.entity.User;
import com.example.users.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.users.infra.dto.EvaluatorAssignmentDTO;

import java.util.List;
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

    // listar posibles evaluadores
    @GetMapping("/evaluators")
    public ResponseEntity<List<User>> listPotentialEvaluators() {
        return ResponseEntity.ok(adminService.listPotentialEvaluators());
    }

    //asignar evaluador
    @PostMapping("/assign-evaluator")
    public ResponseEntity<String> assignEvaluator(@RequestBody EvaluatorAssignmentDTO dto) {
        return ResponseEntity.ok(adminService.assignEvaluator(dto));
    }

    // Obtener evaluadores asignados
    @GetMapping("/assigned-evaluators")
    public ResponseEntity<List<User>> listAssignedEvaluators() {
        return ResponseEntity.ok(adminService.listAssignedEvaluators());
    }
}

