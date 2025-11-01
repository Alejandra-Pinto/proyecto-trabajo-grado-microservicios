package co.unicauca.degreework.controller;

import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.builder.*;
import co.unicauca.degreework.service.DegreeWorkService;
import co.unicauca.degreework.domain.entities.enums.EnumModalidad;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar los trabajos de grado.
 */
@RestController
@RequestMapping("/api/degreeworks")
@CrossOrigin(origins = "*")
public class DegreeWorkController {

    private final DegreeWorkService service;

    public DegreeWorkController(DegreeWorkService service) {
        this.service = service;
    }

    /**
     * Registrar un nuevo trabajo de grado
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarDegreeWork(@RequestBody DegreeWork degreeWork) {
        try {
            DegreeWork nuevo = service.registrarDegreeWork(
                new DegreeWorkBuilder()
                    .titulo(degreeWork.getTitulo())
                    .buildModalidad(degreeWork.getModalidad())
                    .setDirectorProyecto(degreeWork.getDirectorProyecto())
                    .setCodirectoresProyecto(degreeWork.getCodirectoresProyecto())
                    .setEstudiantes(degreeWork.getEstudiantes())
                    .setDescripcion(degreeWork.getDescripcion())
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar el trabajo de grado: " + e.getMessage());
        }
    }

    /**
     * Listar todos los trabajos de grado
     */
    @GetMapping("/listar")
    public ResponseEntity<List<DegreeWork>> listarTodos() {
        List<DegreeWork> lista = service.listarTodos();
        return ResponseEntity.ok(lista);
    }

    /**
     * Listar trabajos de grado asignados a un docente (director o codirector)
     */
    @GetMapping("/docente/{email}")
    public ResponseEntity<List<DegreeWork>> listarPorDocente(@PathVariable String email) {
        List<DegreeWork> lista = service.listarDegreeWorksPorDocente(email);
        return ResponseEntity.ok(lista);
    }

    /**
     * Obtener la información de un trabajo de grado específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        DegreeWork degreeWork = service.obtenerPorId(id);
        if (degreeWork == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trabajo de grado no encontrado");
        }
        return ResponseEntity.ok(degreeWork);
    }

    /**
     * Actualizar información de un trabajo de grado
     */
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizarDegreeWork(@PathVariable Long id, @RequestBody DegreeWork datosActualizados) {
        DegreeWork existente = service.obtenerPorId(id);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trabajo de grado no encontrado");
        }

        // Actualiza solo los campos necesarios
        existente.setTitulo(datosActualizados.getTitulo());
        existente.setModalidad(datosActualizados.getModalidad());
        existente.setDirectorProyecto(datosActualizados.getDirectorProyecto());
        existente.setCodirectoresProyecto(datosActualizados.getCodirectoresProyecto());
        existente.setEstudiantes(datosActualizados.getEstudiantes());
        existente.setCorrecciones(datosActualizados.getCorrecciones());

        DegreeWork actualizado = service.actualizarDegreeWork(existente);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Guardar correcciones del trabajo de grado
     */
    @PutMapping("/{id}/correcciones")
    public ResponseEntity<?> guardarCorrecciones(@PathVariable Long id, @RequestBody String correcciones) {
        try {
            DegreeWork actualizado = service.guardarCorrecciones(id, correcciones);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Eliminar un trabajo de grado
     */
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarDegreeWork(@PathVariable Long id) {
        DegreeWork existente = service.obtenerPorId(id);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trabajo de grado no encontrado");
        }

        service.eliminarDegreeWork(id);
        return ResponseEntity.ok("Trabajo de grado eliminado correctamente.");
    }
}
