package co.unicauca.degreework.controller;

import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import co.unicauca.degreework.infra.dto.ActualizarEvaluacionDTO;
import co.unicauca.degreework.service.DegreeWorkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/degreeworks")
public class DegreeWorkController {

    private final DegreeWorkService service;

    public DegreeWorkController(DegreeWorkService service) {
        this.service = service;
    }

    /**
     * Actualizar estado y observaciones del ÚLTIMO documento
     */
    @PatchMapping("/evaluacion")
    public ResponseEntity<?> actualizarEvaluacion(@RequestBody ActualizarEvaluacionDTO dto) {
        try {
            DegreeWork actualizado = service.actualizarEstadoYObservaciones(dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar la evaluación: " + e.getMessage());
        }
    }

    /**
     * Actualizar solo las observaciones del ÚLTIMO documento
     */
    @PatchMapping("/{id}/observaciones")
    public ResponseEntity<?> actualizarObservaciones(
            @PathVariable Long id,
            @RequestBody String observaciones) {

        try {
            ActualizarEvaluacionDTO dto = new ActualizarEvaluacionDTO();
            dto.setDegreeWorkId(id);
            dto.setObservaciones(observaciones);

            // Estado no cambia → se deja null
            dto.setEstado(null);

            DegreeWork actualizado = service.actualizarEstadoYObservaciones(dto);
            return ResponseEntity.ok(actualizado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Actualizar solo el estado del ÚLTIMO documento
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody EnumEstadoDocument estado) {

        try {
            ActualizarEvaluacionDTO dto = new ActualizarEvaluacionDTO();
            dto.setDegreeWorkId(id);
            dto.setEstado(estado);

            // Observaciones no cambian → se dejan null
            dto.setObservaciones(null);

            DegreeWork actualizado = service.actualizarEstadoYObservaciones(dto);
            return ResponseEntity.ok(actualizado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
