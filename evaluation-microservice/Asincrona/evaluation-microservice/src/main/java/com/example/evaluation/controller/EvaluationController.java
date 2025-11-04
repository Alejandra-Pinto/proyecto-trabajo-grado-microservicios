package com.example.evaluation.controller;

import com.example.evaluation.entity.Evaluation;
import com.example.evaluation.infra.dto.DegreeWorkUpdateDTO;
import com.example.evaluation.infra.dto.EvaluationRequestDTO;
import com.example.evaluation.infra.dto.EvaluationResponseDTO;
import com.example.evaluation.infra.messaging.EvaluationPublisher;
import com.example.evaluation.service.EvaluacionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationPublisher evaluationPublisher;

    private final EvaluacionService evaluacionService;

    public EvaluationController(EvaluacionService evaluacionService, EvaluationPublisher evaluationPublisher) {
        this.evaluacionService = evaluacionService;
        this.evaluationPublisher = evaluationPublisher;
    }

    // Crear evaluación
    @PostMapping
    public EvaluationResponseDTO crearEvaluacion(@RequestBody EvaluationRequestDTO dto) {
        Evaluation evaluacion = evaluacionService.crearEvaluacion(
                dto.getDocumentId(),
                dto.getEvaluadorCorreo(),
                dto.getResultado(),
                dto.getTipo(),
                dto.getCorrecciones());

        EvaluationResponseDTO response = new EvaluationResponseDTO();
        response.setId(evaluacion.getId());
        response.setDocumentId(evaluacion.getDocument().getId());
        response.setResultado(evaluacion.getResultado());
        response.setTipo(evaluacion.getType());
        response.setFechaEvaluacion(evaluacion.getSentAt());
        response.setEvaluadorNombre(evaluacion.getEvaluador().getNombre());
        response.setEvaluadorRol(evaluacion.getEvaluador().getRol());
        response.setEvaluadorCorreo(evaluacion.getEvaluador().getCorreo());
        response.setCorrecciones(dto.getCorrecciones()); // 🔹 correcciones del DTO enviado

        return response;
    }

    // Listar todas las evaluaciones
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
            return dto;
        }).collect(Collectors.toList());
    }

    // Obtener evaluación por correo del evaluador
    @GetMapping("/evaluador/{correo}")
    public List<EvaluationResponseDTO> obtenerEvaluacionesPorCorreo(@PathVariable String correo) {
        return evaluacionService.obtenerPorCorreoEvaluador(correo).stream().map(e -> {
            EvaluationResponseDTO dto = new EvaluationResponseDTO();
            dto.setId(e.getId());
            dto.setDocumentId(e.getDocument().getId());
            dto.setResultado(e.getResultado());
            dto.setTipo(e.getType());
            dto.setFechaEvaluacion(e.getSentAt());
            dto.setEvaluadorNombre(e.getEvaluador().getNombre());
            dto.setEvaluadorRol(e.getEvaluador().getRol());
            dto.setEvaluadorCorreo(e.getEvaluador().getCorreo());
            return dto;
        }).collect(Collectors.toList());
    }

    // Eliminar evaluación
    @DeleteMapping("/{id}")
    public void eliminarEvaluacion(@PathVariable Long id) {
        evaluacionService.eliminarEvaluacion(id);
    }

    @PostMapping("/correcciones")
    public ResponseEntity<String> enviarCorrecciones(@RequestBody DegreeWorkUpdateDTO dto) {
        evaluationPublisher.publicarActualizacionDegreeWork(dto);
        return ResponseEntity.ok("Correcciones enviadas correctamente a la cola.");
    }
}
