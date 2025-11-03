package co.unicauca.degreework.service;

import co.unicauca.degreework.access.*;
import co.unicauca.degreework.domain.entities.*;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkBuilder;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkDirector;
import co.unicauca.degreework.infra.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.infra.dto.DegreeWorkDTO;
import co.unicauca.degreework.infra.dto.DocumentDTO;
import co.unicauca.degreework.infra.messaging.DegreeWorkProducer;

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
    private final DegreeWorkProducer degreeWorkProducer;

    public DegreeWorkService(DegreeWorkRepository repository, UserService userService, DocumentRepository documentRepository, DegreeWorkProducer degreeWorkProducer) {
        this.repository = repository;
        this.userService = userService;
        this.documentRepository = documentRepository;
        this.degreeWorkProducer = degreeWorkProducer;
    }

    /**
     * Registra un nuevo trabajo de grado usando el patrón Builder + Director.
     * Ahora obtiene los usuarios por su email.
     */
    public DegreeWork registrarDegreeWork(DegreeWorkDTO dto, DegreeWorkBuilder builder) {
        DegreeWorkDirector director = new DegreeWorkDirector();
        director.setBuilder(builder);

        // --- Obtener director ---
        User directorProyecto = userService.obtenerPorEmail(dto.getDirectorEmail());
        if (directorProyecto == null) {
            throw new IllegalArgumentException("No se encontró el director con correo: " + dto.getDirectorEmail());
        }

        // --- Obtener estudiantes ---
        List<User> estudiantes = new ArrayList<>();
        for (String email : dto.getEstudiantesEmails()) {
            User estudiante = userService.obtenerPorEmail(email);
            if (estudiante == null) {
                throw new IllegalArgumentException("No se encontró el estudiante con correo: " + email);
            }
            estudiantes.add(estudiante);
        }

        // --- Obtener codirectores ---
        List<User> codirectores = new ArrayList<>();
        if (dto.getCodirectoresEmails() != null) {
            for (String email : dto.getCodirectoresEmails()) {
                User codirector = userService.obtenerPorEmail(email);
                if (codirector == null) {
                    throw new IllegalArgumentException("No se encontró el codirector con correo: " + email);
                }
                codirectores.add(codirector);
            }
        }

        // --- Configurar el builder ---
        builder.titulo(dto.getTitulo())
                .director(directorProyecto)
                .objetivoGeneral(dto.getObjetivoGeneral())
                .objetivosEspecificos(dto.getObjetivosEspecificos())
                .fechaActual(dto.getFechaActual())
                .estadoInicial(dto.getEstado());
                
        // --- Agregar estudiantes y codirectores ---
        estudiantes.forEach(builder::agregarEstudiante);
        codirectores.forEach(builder::agregarCodirector);

        // --- Cargar documentos desde DTOs ---
        builder.documentosDesdeDTOs(dto.getFormatosA());
        builder.documentosDesdeDTOs(dto.getAnteproyectos());
        builder.documentosDesdeDTOs(dto.getCartasAceptacion());

        // --- Construir el trabajo final ---
        DegreeWork degreeWork = director.construirTrabajo();
        DegreeWork saved = repository.save(degreeWork);

        //Enviar evento a RabbitMQ
        DegreeWorkCreatedEvent event = new DegreeWorkCreatedEvent(
                saved.getTitulo(),
                saved.getModalidad().name(),
                directorProyecto.getEmail(),
                estudiantes.stream().map(User::getEmail).collect(Collectors.toList()),
                codirectores.stream().map(User::getEmail).collect(Collectors.toList()),
                saved.getFechaActual(),
                saved.getEstado().name(),
                dto.getFormatosA(),
                dto.getAnteproyectos(),
                dto.getCartasAceptacion()
        );
        degreeWorkProducer.sendDegreeWorkCreated(event);

        return saved;
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
     * Actualizar un trabajo de grado (usando emails)
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
        existente.setModalidad(dto.getModalidad());

        if (dto.getObjetivosEspecificos() != null) {
            existente.setObjetivosEspecificos(new ArrayList<>(dto.getObjetivosEspecificos()));
        }

        // --- Actualizar director ---
        if (dto.getDirectorEmail() != null) {
            User nuevoDirector = userService.obtenerPorEmail(dto.getDirectorEmail());
            if (nuevoDirector == null) {
                throw new IllegalArgumentException("No se encontró el director con correo: " + dto.getDirectorEmail());
            }
            existente.setDirectorProyecto(nuevoDirector);
        }

        // --- Actualizar estudiantes ---
        if (dto.getEstudiantesEmails() != null && !dto.getEstudiantesEmails().isEmpty()) {
            List<User> estudiantes = dto.getEstudiantesEmails().stream()
                    .map(userService::obtenerPorEmail)
                    .collect(Collectors.toList());
            existente.setEstudiantes(estudiantes);
        }

        // --- Actualizar codirectores ---
        if (dto.getCodirectoresEmails() != null && !dto.getCodirectoresEmails().isEmpty()) {
            List<User> codirectores = dto.getCodirectoresEmails().stream()
                    .map(userService::obtenerPorEmail)
                    .collect(Collectors.toList());
            existente.setCodirectoresProyecto(codirectores);
        }

        // --- Actualizar documentos ---
        actualizarDocumentos(existente.getFormatosA(), dto.getFormatosA());
        actualizarDocumentos(existente.getCartasAceptacion(), dto.getCartasAceptacion());
        actualizarDocumentos(existente.getAnteproyectos(), dto.getAnteproyectos());

        DegreeWork saved = repository.save(existente);

        //Enviar evento a RabbitMQ
        DegreeWorkCreatedEvent event = new DegreeWorkCreatedEvent(
                saved.getTitulo(),
                saved.getModalidad().name(),
                saved.getDirectorProyecto().getEmail(),
                saved.getEstudiantes().stream().map(User::getEmail).collect(Collectors.toList()),
                saved.getCodirectoresProyecto().stream().map(User::getEmail).collect(Collectors.toList()),
                saved.getFechaActual(),
                saved.getEstado().name(),
                dto.getFormatosA(),
                dto.getAnteproyectos(),
                dto.getCartasAceptacion()
        );
        degreeWorkProducer.sendDegreeWorkCreated(event);

        return saved;
    }

    /**
     * Método auxiliar para actualizar documentos
     */
    private void actualizarDocumentos(List<Document> existentes, List<DocumentDTO> nuevos) {
        if (nuevos != null) {
            existentes.clear();
            for (DocumentDTO docDto : nuevos) {
                Document doc = new Document();
                doc.setTipo(docDto.getTipo());
                doc.setEstado(docDto.getEstado());
                doc.setRutaArchivo(docDto.getRutaArchivo());
                existentes.add(doc);
            }
        }
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