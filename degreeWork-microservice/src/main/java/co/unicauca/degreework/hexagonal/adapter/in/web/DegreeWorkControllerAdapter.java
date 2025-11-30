package co.unicauca.degreework.hexagonal.adapter.in.web;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkDTO;
import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.hexagonal.application.dto.EvaluacionEventDTO;
import co.unicauca.degreework.hexagonal.application.service.*;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.hexagonal.port.in.web.DegreeWorkControllerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/degreeworks")
public class DegreeWorkControllerAdapter implements DegreeWorkControllerPort {

    private final CreateDegreeWorkUseCase createDegreeWorkUseCase;
    private final GetDegreeWorkUseCase getDegreeWorkUseCase;
    private final UpdateDegreeWorkUseCase updateDegreeWorkUseCase;
    private final DeleteDegreeWorkUseCase deleteDegreeWorkUseCase;
    private final DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase;

    @Autowired
    public DegreeWorkControllerAdapter(
            CreateDegreeWorkUseCase createDegreeWorkUseCase,
            GetDegreeWorkUseCase getDegreeWorkUseCase,
            UpdateDegreeWorkUseCase updateDegreeWorkUseCase,
            DeleteDegreeWorkUseCase deleteDegreeWorkUseCase,
            DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase) {
        this.createDegreeWorkUseCase = createDegreeWorkUseCase;
        this.getDegreeWorkUseCase = getDegreeWorkUseCase;
        this.updateDegreeWorkUseCase = updateDegreeWorkUseCase;
        this.deleteDegreeWorkUseCase = deleteDegreeWorkUseCase;
        this.degreeWorkEvaluationUseCase = degreeWorkEvaluationUseCase;
    }

    @Override
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarDegreeWork(@RequestBody DegreeWorkDTO dto) {
        try {
            DegreeWork nuevo = createDegreeWorkUseCase.execute(dto);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Override
    @GetMapping
    public ResponseEntity<List<DegreeWork>> listarTodos() {
        List<DegreeWork> result = getDegreeWorkUseCase.findAll();
        return ResponseEntity.ok(result);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<DegreeWork> obtenerPorId(@PathVariable Long id) {
        DegreeWork degreeWork = getDegreeWorkUseCase.findById(id).orElse(null);
        if (degreeWork == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(degreeWork);
    }

    @Override
    @GetMapping("/docente/{email}")
    public ResponseEntity<List<DegreeWork>> listarPorDocente(@PathVariable String email) {
        List<DegreeWork> trabajos = getDegreeWorkUseCase.findByTeacher(email);
        return ResponseEntity.ok(trabajos);
    }

    @Override
    @GetMapping("/estudiante/{email}")
    public ResponseEntity<List<DegreeWork>> listarPorEstudiante(@PathVariable String email) {
        List<DegreeWork> trabajos = getDegreeWorkUseCase.findByStudent(email);
        return ResponseEntity.ok(trabajos);
    }

    @Override
    @GetMapping("/listar/{estado}")
    public ResponseEntity<List<DegreeWork>> listarPorEstado(@PathVariable EnumEstadoDegreeWork estado) {
        List<DegreeWork> trabajos = getDegreeWorkUseCase.findByEstado(estado);
        return ResponseEntity.ok(trabajos);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<DegreeWork> actualizarDegreeWork(
            @PathVariable Long id,
            @RequestBody DegreeWorkDTO dto) {
        try {
            DegreeWork actualizado = updateDegreeWorkUseCase.execute(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDegreeWork(@PathVariable Long id) {
        try {
            deleteDegreeWorkUseCase.execute(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PutMapping("/evaluaciones/actualizar")
    public ResponseEntity<Void> actualizarDesdeEvaluacion(@RequestBody DegreeWorkUpdateDTO dto) {
        try {
            degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @PostMapping("/evaluaciones/asignar-evaluadores")
    public ResponseEntity<Void> asignarEvaluadores(@RequestBody EvaluacionEventDTO dto) {
        try {
            degreeWorkEvaluationUseCase.asignarEvaluadores(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}