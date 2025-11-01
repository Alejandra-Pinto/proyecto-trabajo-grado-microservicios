package co.unicauca.degreework.service;

import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumModalidad;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkBuilder;
import co.unicauca.degreework.access.DegreeWorkRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DegreeWorkService {

    private final DegreeWorkRepository repository;

    public DegreeWorkService(DegreeWorkRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra un nuevo trabajo de grado usando el patrón Builder
     */
    public DegreeWork registrarDegreeWork(DegreeWorkBuilder builder) {
        DegreeWork degreeWork = builder.build();

        // Validar modalidad y cantidad de estudiantes
        if (degreeWork.getModalidad() == EnumModalidad.PRACTICA_PROFESIONAL 
                && degreeWork.getEstudiantes().size() != 1) {
            throw new IllegalArgumentException("La práctica profesional debe tener exactamente un estudiante.");
        }

        if (degreeWork.getModalidad() == EnumModalidad.INVESTIGACION 
                && degreeWork.getEstudiantes().size() > 2) {
            throw new IllegalArgumentException("La modalidad de investigación puede tener máximo dos estudiantes.");
        }

        return repository.save(degreeWork);
    }

    /**
     * Obtener un trabajo de grado por ID
     */
    public DegreeWork obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * Listar todos los trabajos de grado
     */
    public List<DegreeWork> listarTodos() {
        return repository.findAll();
    }

    /**
     * Listar trabajos de grado asignados a un docente (director o codirector)
     */
    public List<DegreeWork> listarDegreeWorksPorDocente(String teacherEmail) {
        return repository.listByTeacher(teacherEmail);
    }

    /**
     * Actualizar un trabajo de grado
     */
    public DegreeWork actualizarDegreeWork(DegreeWork degreeWork) {
        return repository.save(degreeWork);
    }

    /**
     * Eliminar un trabajo de grado
     */
    public void eliminarDegreeWork(Long id) {
        repository.deleteById(id);
    }

    /**
     * Guardar correcciones del trabajo de grado
     */
    public DegreeWork guardarCorrecciones(Long id, String correcciones) {
        DegreeWork degreeWork = obtenerPorId(id);
        if (degreeWork == null) {
            throw new IllegalArgumentException("No se encontró el trabajo de grado con ID " + id);
        }

        degreeWork.setCorrecciones(correcciones);
        return repository.save(degreeWork);
    }
}
