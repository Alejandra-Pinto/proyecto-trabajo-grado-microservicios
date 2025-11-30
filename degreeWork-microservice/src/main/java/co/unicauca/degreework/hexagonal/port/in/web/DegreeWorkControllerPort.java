package co.unicauca.degreework.hexagonal.port.in.web;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkDTO;
import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.hexagonal.application.dto.EvaluacionEventDTO;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DegreeWorkControllerPort {
    ResponseEntity<?> registrarDegreeWork(DegreeWorkDTO dto);
    ResponseEntity<List<DegreeWork>> listarTodos();
    ResponseEntity<DegreeWork> obtenerPorId(Long id);
    ResponseEntity<List<DegreeWork>> listarPorDocente(String email);
    ResponseEntity<List<DegreeWork>> listarPorEstudiante(String email);
    ResponseEntity<List<DegreeWork>> listarPorEstado(EnumEstadoDegreeWork estado);
    ResponseEntity<DegreeWork> actualizarDegreeWork(Long id, DegreeWorkDTO dto);
    ResponseEntity<Void> eliminarDegreeWork(Long id);
    ResponseEntity<Void> actualizarDesdeEvaluacion(DegreeWorkUpdateDTO dto);
    ResponseEntity<Void> asignarEvaluadores(EvaluacionEventDTO dto);
}