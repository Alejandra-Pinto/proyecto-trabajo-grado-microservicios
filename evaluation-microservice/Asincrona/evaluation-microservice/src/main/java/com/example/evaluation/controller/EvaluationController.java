package com.example.evaluation.controller;

import com.example.evaluation.entity.Evaluation;
import com.example.evaluation.infra.dto.EvaluationRequestDTO;
import com.example.evaluation.infra.dto.EvaluationResponseDTO;
import com.example.evaluation.service.EvaluacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluacionService evaluacionService;

    public EvaluationController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    // ✅ Crear evaluación
    @PostMapping
    public EvaluationResponseDTO crearEvaluacion(@RequestBody EvaluationRequestDTO dto) {
        Evaluation evaluacion = evaluacionService.crearEvaluacion(
                dto.getDocumentId(),
                dto.getEvaluadorCorreo(), // ← ahora usamos el correo
                dto.getResultado(),
                dto.getTipo());

        EvaluationResponseDTO response = new EvaluationResponseDTO();
        response.setId(evaluacion.getId());
        response.setDocumentId(evaluacion.getDocument().getId());
        response.setResultado(evaluacion.getResultado());
        response.setTipo(evaluacion.getType());
        response.setFechaEvaluacion(evaluacion.getSentAt());
        response.setEvaluadorNombre(evaluacion.getEvaluador().getNombre());
        response.setEvaluadorRol(evaluacion.getEvaluador().getRol());
        response.setEvaluadorCorreo(evaluacion.getEvaluador().getCorreo());

        if (evaluacion.getDocument().getDegreeWork() != null) {
            response.setCorrecciones(evaluacion.getDocument().getDegreeWork().getCorrecciones());
        }

        return response;
    }

    // ✅ Listar todas las evaluaciones
    @GetMapping
    public List<EvaluationResponseDTO> listarEvaluaciones() {
        return evaluacionService.listarEvaluaciones().stream().map(e -> {
            EvaluationResponseDTO dto = new EvaluationResponseDTO();
            dto.setId(e.getId());
            dto.setDocumentId(e.getDocument().getId());
            dto.setResultado(e.getResultado());
            dto.setTipo(e.getType());
            dto.setFechaEvaluacion(e.getSentAt());
            dto.setEvaluadorNombre(e.getEvaluador().getNombre());
            dto.setEvaluadorRol(e.getEvaluador().getRol());
            dto.setEvaluadorCorreo(e.getEvaluador().getCorreo());

            if (e.getDocument().getDegreeWork() != null) {
                dto.setCorrecciones(e.getDocument().getDegreeWork().getCorrecciones());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // ✅ Obtener evaluación por correo del evaluador
    @GetMapping("/evaluador/{correo}")
    public List<EvaluationResponseDTO> obtenerEvaluacionesPorCorreo(@PathVariable String correo) {
        List<Evaluation> evaluaciones = evaluacionService.obtenerPorCorreoEvaluador(correo);

        return evaluaciones.stream().map(e -> {
            EvaluationResponseDTO dto = new EvaluationResponseDTO();
            dto.setId(e.getId());
            dto.setDocumentId(e.getDocument().getId());
            dto.setResultado(e.getResultado());
            dto.setTipo(e.getType());
            dto.setFechaEvaluacion(e.getSentAt());
            dto.setEvaluadorNombre(e.getEvaluador().getNombre());
            dto.setEvaluadorRol(e.getEvaluador().getRol());
            dto.setEvaluadorCorreo(e.getEvaluador().getCorreo());
            if (e.getDocument().getDegreeWork() != null) {
                dto.setCorrecciones(e.getDocument().getDegreeWork().getCorrecciones());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // ✅ Eliminar evaluación (si sigue siendo por id interno)
    @DeleteMapping("/{id}")
    public void eliminarEvaluacion(@PathVariable Long id) {
        evaluacionService.eliminarEvaluacion(id);
    }
}
