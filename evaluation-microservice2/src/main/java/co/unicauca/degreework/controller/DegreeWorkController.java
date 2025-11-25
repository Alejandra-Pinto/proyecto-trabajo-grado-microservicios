package co.unicauca.degreework.controller;

import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkBuilder;
import co.unicauca.degreework.domain.entities.builder.ProfessionalPracticeBuilder;
import co.unicauca.degreework.domain.entities.builder.ResearchDegreeWorkBuilder;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.infra.dto.DegreeWorkDTO;
import co.unicauca.degreework.infra.dto.ActualizarEvaluacionDTO;
import co.unicauca.degreework.service.DegreeWorkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar los trabajos de grado.
 * Permite registrar, actualizar, listar, consultar y eliminar trabajos.
 */
@RestController
@RequestMapping("/api/degreeworks")
public class DegreeWorkController {

    private final DegreeWorkService service;

    public DegreeWorkController(DegreeWorkService service) {
        this.service = service;
    }

    /**
     * Endpoint específico para que los evaluadores actualicen estado y observaciones
     * sin modificar otra información del trabajo de grado
     */
    @PatchMapping("/evaluacion")
    public ResponseEntity<?> actualizarEvaluacion(@RequestBody ActualizarEvaluacionDTO dto) {
        try {
            DegreeWork actualizado = service.actualizarEstadoYObservaciones(dto);
            return ResponseEntity.ok(actualizado);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar la evaluación: " + e.getMessage());
        }
    }

    /**
     * Actualizar solo las observaciones de un trabajo de grado
     */
    @PatchMapping("/{id}/observaciones")
    public ResponseEntity<?> actualizarObservaciones(@PathVariable Long id, @RequestBody String observaciones) {
        try {
            ActualizarEvaluacionDTO dto = new ActualizarEvaluacionDTO();
            dto.setDegreeWorkId(id);
            dto.setObservaciones(observaciones);
            // Mantener el estado actual
            dto.setEstado(service.obtenerPorId(id).getEstado());
            
            DegreeWork actualizado = service.actualizarEstadoYObservaciones(dto);
            return ResponseEntity.ok(actualizado);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Actualizar solo el estado de un trabajo de grado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody EnumEstadoDegreeWork estado) {
        try {
            ActualizarEvaluacionDTO dto = new ActualizarEvaluacionDTO();
            dto.setDegreeWorkId(id);
            dto.setEstado(estado);
            // Mantener las observaciones actuales
            dto.setObservaciones(service.obtenerPorId(id).getCorrecciones());
            
            DegreeWork actualizado = service.actualizarEstadoYObservaciones(dto);
            return ResponseEntity.ok(actualizado);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
