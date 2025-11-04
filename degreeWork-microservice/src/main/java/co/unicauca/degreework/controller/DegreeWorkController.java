package co.unicauca.degreework.controller;

import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkBuilder;
import co.unicauca.degreework.domain.entities.builder.ProfessionalPracticeBuilder;
import co.unicauca.degreework.domain.entities.builder.ResearchDegreeWorkBuilder;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.infra.dto.DegreeWorkDTO;
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
     * Registrar un nuevo trabajo de grado
     * Se recibe un DTO y el tipo de builder que se usará (por ejemplo, investigación o práctica)
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarDegreeWork(@RequestBody DegreeWorkDTO dto) {
        try {
            DegreeWorkBuilder builder;

            // Seleccionar builder según modalidad
            switch (dto.getModalidad()) {
                case INVESTIGACION -> builder = new ResearchDegreeWorkBuilder();
                case PRACTICA_PROFESIONAL -> builder = new ProfessionalPracticeBuilder();
                default -> throw new IllegalArgumentException("Modalidad no soportada: " + dto.getModalidad());
            }

            DegreeWork nuevo = service.registrarDegreeWork(dto, builder);
            return ResponseEntity.ok(nuevo);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtener todos los trabajos de grado
     */
    @GetMapping
    public ResponseEntity<List<DegreeWork>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    /**
     * Obtener información de un trabajo de grado específico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DegreeWork> obtenerPorId(@PathVariable Long id) {
        DegreeWork degreeWork = service.obtenerPorId(id);
        if (degreeWork == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(degreeWork);
    }

    /**
     * Listar trabajos de grado asociados a un docente (como director)
     */
    @GetMapping("/docente/{email}")
    public ResponseEntity<List<DegreeWork>> listarPorDocente(@PathVariable String email) {
        List<DegreeWork> trabajos = service.listarDegreeWorksPorDocente(email);
        return ResponseEntity.ok(trabajos);
    }

    /**
     * Listar trabajos de grado asociados a un estudiante
     */
    @GetMapping("/estudiante/{email}")
    public ResponseEntity<List<DegreeWork>> listarPorEstudiante(@PathVariable String email) {
        List<DegreeWork> trabajos = service.listarDegreeWorksPorEstudiante(email);
        return ResponseEntity.ok(trabajos);
    }

    /**
     * Listar trabajos por estado
     */
    @GetMapping("/listar/{estado}")
    public ResponseEntity<List<DegreeWork>> listarPorEstado(@PathVariable EnumEstadoDegreeWork estado) {
        List<DegreeWork> trabajos = service.listarAnteproyectos(estado);
        return ResponseEntity.ok(trabajos);
    }

    /**
     * Actualizar información de un trabajo de grado
     */
    @PutMapping("/{id}")
    public ResponseEntity<DegreeWork> actualizarDegreeWork(
            @PathVariable Long id,
            @RequestBody DegreeWorkDTO dto) {
        try {
            DegreeWork actualizado = service.actualizarDegreeWork(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Eliminar un trabajo de grado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDegreeWork(@PathVariable Long id) {
        service.eliminarDegreeWork(id);
        return ResponseEntity.noContent().build();
    }
}
