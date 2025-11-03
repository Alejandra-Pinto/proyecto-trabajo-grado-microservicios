package com.example.submission.controller;

import com.example.submission.entity.DegreeWork;
import com.example.submission.infra.dto.DegreeWorkRequest;
import com.example.submission.service.IDegreeWorkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/degreeworks")
public class DegreeWorkController {

    private final IDegreeWorkService service;

    public DegreeWorkController(IDegreeWorkService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DegreeWork> create(@RequestBody DegreeWorkRequest request) {
        DegreeWork created = service.createDegreeWork(request);
        return ResponseEntity.ok(created);
    }
    @GetMapping
    public List<DegreeWork> getAll() {
        return service.getAllDegreeWorks();
    }

    @GetMapping("/{id}")
    public DegreeWork getById(@PathVariable Long id) {
        return service.getDegreeWorkById(id);
    }
}
