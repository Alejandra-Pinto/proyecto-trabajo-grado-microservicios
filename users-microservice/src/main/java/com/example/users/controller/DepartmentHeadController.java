package com.example.users.controller;

import com.example.users.infra.dto.EvaluatorAssignmentDTO;
import com.example.users.entity.User;
import com.example.users.service.DepartmentHeadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/department-head")
public class DepartmentHeadController {

    private final DepartmentHeadService departmentHeadService;

    public DepartmentHeadController(DepartmentHeadService departmentHeadService) {
        this.departmentHeadService = departmentHeadService;
    }

    // GET → listar potenciales evaluadores
    @GetMapping("/evaluators/potential")
    public ResponseEntity<List<User>> listPotentialEvaluators() {
        List<User> evaluators = departmentHeadService.listPotentialEvaluators();
        return ResponseEntity.ok(evaluators);
    }

    // POST → asignar evaluador
    @PostMapping("/evaluators/assign")
    public ResponseEntity<String> assignEvaluator(@RequestBody EvaluatorAssignmentDTO dto) {
        String message = departmentHeadService.assignEvaluator(dto);
        return ResponseEntity.ok(message);
    }
}
