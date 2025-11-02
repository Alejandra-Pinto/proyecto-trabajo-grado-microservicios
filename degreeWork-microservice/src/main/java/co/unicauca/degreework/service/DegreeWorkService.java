package co.unicauca.degreework.service;

import co.unicauca.degreework.access.*;
import co.unicauca.degreework.domain.entities.*;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkBuilder;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkDirector;
import co.unicauca.degreework.infra.dto.DegreeWorkDTO;
import co.unicauca.degreework.infra.dto.DocumentDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DegreeWorkService {

    private final DegreeWorkRepository repository;
    private final UserService userService;
    private final DocumentRepository documentRepository;

    public DegreeWorkService(DegreeWorkRepository repository, UserService userService, DocumentRepository documentRepository) {
        this.repository = repository;
        this.userService = userService;
        this.documentRepository = documentRepository; // 👈
    }

    /**
     * Registra un nuevo trabajo de grado usando el patrón Builder + Director.
     */
    public DegreeWork registrarDegreeWork(DegreeWorkDTO dto, DegreeWorkBuilder builder) {
        DegreeWorkDirector director = new DegreeWorkDirector();
        director.setBuilder(builder);

        // Convertir DTO a entidades
        User directorProyecto = userService.obtenerPorId(dto.getDirectorId());
        List<User> estudiantes = new ArrayList<>();
        for (Integer id : dto.getEstudiantesIds()) {
            estudiantes.add(userService.obtenerPorId(id.longValue()));
        }

        List<User> codirectores = new ArrayList<>();
        if (dto.getCodirectoresIds() != null) {
            for (Integer id : dto.getCodirectoresIds()) {
                codirectores.add(userService.obtenerPorId(id.longValue()));
            }
        }

        // Configurar el builder
        builder.titulo(dto.getTitulo())
                .director(directorProyecto)
                .objetivoGeneral(dto.getObjetivoGeneral())
                .objetivosEspecificos(dto.getObjetivosEspecificos())
                .fechaActual(dto.getFechaActual())
                .estadoInicial(dto.getEstado());

        // Agregar estudiantes (valida internamente según modalidad)
        for (User estudiante : estudiantes) {
            builder.agregarEstudiante(estudiante);
        }

        // Agregar codirectores
        for (User codirector : codirectores) {
            builder.agregarCodirector(codirector);
        }

        // Construir el trabajo final
        DegreeWork degreeWork = director.construirTrabajo();

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
     * Listar trabajos por docente (director o codirector)
     */
    public List<DegreeWork> listarDegreeWorksPorDocente(String teacherEmail) {
        return repository.listByTeacher(teacherEmail);
    }

    /**
     * Actualizar un trabajo de grado
     */
    @Transactional
    public DegreeWork actualizarDegreeWork(Long id, DegreeWorkDTO dto) {
        DegreeWork existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el trabajo de grado con ID " + id));

        // --- Actualizar datos básicos ---
        existente.setTitulo(dto.getTitulo());
        existente.setObjetivoGeneral(dto.getObjetivoGeneral());
        existente.setCorrecciones(dto.getCorrecciones());
        existente.setEstado(dto.getEstado());
        existente.setFechaActual(dto.getFechaActual());

        if (dto.getObjetivosEspecificos() != null) {
            existente.setObjetivosEspecificos(new ArrayList<>(dto.getObjetivosEspecificos()));
        } else {
            existente.setObjetivosEspecificos(new ArrayList<>());
        }

        // --- Actualizar director ---
        if (dto.getDirectorId() != null) {
            existente.setDirectorProyecto(userService.obtenerPorId(dto.getDirectorId()));
        }

        // --- Actualizar estudiantes ---
        if (dto.getEstudiantesIds() != null && !dto.getEstudiantesIds().isEmpty()) {
            List<User> estudiantes = dto.getEstudiantesIds().stream()
                    .map(idEst -> userService.obtenerPorId(idEst.longValue()))
                    .collect(Collectors.toList());
            existente.setEstudiantes(estudiantes);
        } else if (existente.getEstudiantes() == null) {
            existente.setEstudiantes(new ArrayList<>());
        }

        // --- Actualizar codirectores ---
        if (dto.getCodirectoresIds() != null && !dto.getCodirectoresIds().isEmpty()) {
            List<User> codirectores = dto.getCodirectoresIds().stream()
                    .map(idCod -> userService.obtenerPorId(idCod.longValue()))
                    .collect(Collectors.toList());
            existente.setCodirectoresProyecto(codirectores);
        } else if (existente.getCodirectoresProyecto() == null) {
            existente.setCodirectoresProyecto(new ArrayList<>());
        }

        // --- Actualizar formatos A ---
        if (dto.getFormatosA() != null) {
            existente.getFormatosA().clear(); // mantener la misma referencia
            for (DocumentDTO docDto : dto.getFormatosA()) {
                Document doc = new Document();
                doc.setTipo(docDto.getTipoDocumento());
                doc.setEstado(docDto.getEstado());
                doc.setRutaArchivo(docDto.getRutaArchivo());
                existente.getFormatosA().add(doc);
            }
        }

        // --- Actualizar cartas de aceptación ---
        if (dto.getCartasAceptacion() != null) {
            existente.getCartasAceptacion().clear();
            for (DocumentDTO docDto : dto.getCartasAceptacion()) {
                Document doc = new Document();
                doc.setTipo(docDto.getTipoDocumento());
                doc.setEstado(docDto.getEstado());
                doc.setRutaArchivo(docDto.getRutaArchivo());
                existente.getCartasAceptacion().add(doc);
            }
        }

        // --- Actualizar anteproyectos ---
        if (dto.getAnteproyectos() != null) {
            existente.getAnteproyectos().clear();
            for (DocumentDTO docDto : dto.getAnteproyectos()) {
                Document doc = new Document();
                doc.setTipo(docDto.getTipoDocumento());
                doc.setEstado(docDto.getEstado());
                doc.setRutaArchivo(docDto.getRutaArchivo());
                existente.getAnteproyectos().add(doc);
            }
        }

        // --- Guardar cambios ---
        return repository.save(existente);
    }

    /**
     * Eliminar un trabajo de grado
     */
    public void eliminarDegreeWork(Long id) {
        repository.deleteById(id);
    }

    /**
     * Guardar correcciones
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
