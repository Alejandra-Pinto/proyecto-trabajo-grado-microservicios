package com.example.evaluation.controller;

import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.service.DegreeWorkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/degreeworks")
public class DegreeWorkController {

    private final DegreeWorkService degreeWorkService;

    public DegreeWorkController(DegreeWorkService degreeWorkService) {
        this.degreeWorkService = degreeWorkService;
    }

    // Listar solo los anteproyectos
    @GetMapping("/anteproyectos")
    public List<DegreeWork> listarAnteproyectos() {
        return degreeWorkService.listarAnteproyectos();
    }

    // Listar todos los trabajos de grado
    @GetMapping
    public List<DegreeWork> listarTodos() {
        return degreeWorkService.listarTodos();
    }

    // Obtener un trabajo de grado específico
    @GetMapping("/{id}")
    public DegreeWork obtenerPorId(@PathVariable Integer id) {
        return degreeWorkService.obtenerPorId(id);
    }

    // Asignar evaluadores (por parte del jefe de departamento)
    @PostMapping("/{id}/asignar-evaluadores")
    public DegreeWork asignarEvaluadores(
            @PathVariable Integer id,
            @RequestParam Long evaluador1Id,
            @RequestParam Long evaluador2Id) {
        return degreeWorkService.asignarEvaluadores(id, evaluador1Id, evaluador2Id);
    }
}
