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

    // ✅ Listar solo los anteproyectos
    @GetMapping("/anteproyectos")
    public List<DegreeWork> listarAnteproyectos() {
        return degreeWorkService.listarAnteproyectos();
    }

    // ✅ Listar todos los trabajos de grado
    @GetMapping
    public List<DegreeWork> listarTodos() {
        return degreeWorkService.listarTodos();
    }

    // ✅ Obtener un trabajo de grado por correo del estudiante
    @GetMapping("/correo/{correo}")
    public DegreeWork obtenerPorCorreo(@PathVariable String correo) {
        return degreeWorkService.obtenerPorCorreo(correo);
    }

    // ✅ Asignar evaluadores (usando sus correos)
    @PostMapping("/{id}/asignar-evaluadores")
    public DegreeWork asignarEvaluadores(
            @PathVariable Integer id,
            @RequestParam String correoEvaluador1,
            @RequestParam String correoEvaluador2) {
        return degreeWorkService.asignarEvaluadoresPorCorreo(id, correoEvaluador1, correoEvaluador2);
    }
}
