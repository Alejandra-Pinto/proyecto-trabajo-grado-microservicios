package co.unicauca.degreework.service;

import co.unicauca.degreework.access.*;
import co.unicauca.degreework.domain.entities.*;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkBuilder;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkDirector;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import co.unicauca.degreework.domain.entities.enums.EnumTipoDocumento;
import co.unicauca.degreework.domain.entities.memento.DegreeWorkCaretaker;
import co.unicauca.degreework.domain.entities.memento.DegreeWorkMemento;
import co.unicauca.degreework.domain.entities.memento.DegreeWorkOriginator;
import co.unicauca.degreework.infra.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.infra.dto.DegreeWorkDTO;
import co.unicauca.degreework.infra.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.infra.dto.DocumentDTO;
import co.unicauca.degreework.infra.dto.NotificationEventDTO;
import co.unicauca.degreework.infra.messaging.DegreeWorkProducer;
import co.unicauca.degreework.infra.messaging.NotificationProducer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final NotificationProducer notificationProducer;

    public DegreeWorkService(DegreeWorkRepository repository, UserService userService,
                            DocumentRepository documentRepository,
                            DegreeWorkProducer degreeWorkProducer,
                            NotificationProducer notificationProducer) {
        this.repository = repository;
        this.userService = userService;
        this.documentRepository = documentRepository;
        this.degreeWorkProducer = degreeWorkProducer;
        this.notificationProducer = notificationProducer;
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

            // 🔍 Validar que no tenga trabajos con documentos no rechazados
            List<DegreeWork> trabajos = repository.listByStudent(email);

            boolean tieneDocumentoActivo = trabajos.stream().anyMatch(dw -> {
                // Revisa todos los documentos asociados
                List<Document> todosDocs = new ArrayList<>();
                if (dw.getFormatosA() != null) todosDocs.addAll(dw.getFormatosA());
                if (dw.getAnteproyectos() != null) todosDocs.addAll(dw.getAnteproyectos());
                if (dw.getCartasAceptacion() != null) todosDocs.addAll(dw.getCartasAceptacion());

                // Si hay documentos y el último no está rechazado, retorna true
                if (!todosDocs.isEmpty()) {
                    Document ultimo = todosDocs.get(todosDocs.size() - 1);
                    return ultimo.getEstado() != EnumEstadoDocument.RECHAZADO;
                }
                return false;
            });

            if (tieneDocumentoActivo) {
                throw new IllegalStateException(
                        "El estudiante con correo " + email +
                        " ya tiene un trabajo con documentos activos (no rechazados) y no puede registrar otro."
                );
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

        // Evento original (para evaluation, etc.)
        DegreeWorkCreatedEvent event = new DegreeWorkCreatedEvent(
            saved.getId(),
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

        // NUEVO: enviar notificación al microservicio notification
        NotificationEventDTO notificationEvent = new NotificationEventDTO(
            "TRABAJO_GRADO_REGISTRADO",
            saved.getTitulo(),
            saved.getModalidad().name(),
            estudiantes.isEmpty() ? null : estudiantes.get(0).getEmail(),
            directorProyecto.getEmail(),
            codirectores.size() > 0 ? codirectores.get(0).getEmail() : null,
            codirectores.size() > 1 ? codirectores.get(1).getEmail() : null
        );

        notificationProducer.sendNotification(notificationEvent);

        return saved;
    }

    @Transactional
    public DegreeWork actualizarDegreeWork(Long id, DegreeWorkDTO dto) {
        DegreeWork existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el trabajo de grado con ID " + id));

        // === INICIO MEMENTO ===
        DegreeWorkOriginator originator = new DegreeWorkOriginator(existente);
        DegreeWorkCaretaker caretaker = new DegreeWorkCaretaker();

        caretaker.addMemento(originator.save());
        System.out.println("[MEMENTO] Estado previo del trabajo de grado con ID " + id + " guardado correctamente.");
        // === FIN MEMENTO ===

        try {
            // --- Actualización de la información general ---
            existente.setTitulo(dto.getTitulo());
            existente.setObjetivoGeneral(dto.getObjetivoGeneral());
            existente.setCorrecciones(dto.getCorrecciones());
            existente.setEstado(dto.getEstado());
            existente.setFechaActual(dto.getFechaActual());
            existente.setModalidad(dto.getModalidad());

            if (dto.getObjetivosEspecificos() != null) {
                existente.setObjetivosEspecificos(new ArrayList<>(dto.getObjetivosEspecificos()));
            }

            if (dto.getDirectorEmail() != null) {
                User nuevoDirector = userService.obtenerPorEmail(dto.getDirectorEmail());
                if (nuevoDirector == null) {
                    throw new IllegalArgumentException("No se encontró el director con correo: " + dto.getDirectorEmail());
                }
                existente.setDirectorProyecto(nuevoDirector);
            }

            if (dto.getEstudiantesEmails() != null && !dto.getEstudiantesEmails().isEmpty()) {
                List<User> estudiantes = dto.getEstudiantesEmails().stream()
                        .map(userService::obtenerPorEmail)
                        .collect(Collectors.toList());
                existente.setEstudiantes(estudiantes);
            }

            if (dto.getCodirectoresEmails() != null && !dto.getCodirectoresEmails().isEmpty()) {
                List<User> codirectores = dto.getCodirectoresEmails().stream()
                        .map(userService::obtenerPorEmail)
                        .collect(Collectors.toList());
                existente.setCodirectoresProyecto(codirectores);
            }

            // --- Actualizar documentos ---
            actualizarDocumentos(dto, existente);

            // --- Guardar cambios ---
            DegreeWork saved = repository.save(existente);

            // --- Enviar evento ---
            DegreeWorkCreatedEvent event = new DegreeWorkCreatedEvent(
                saved.getId(),
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

            System.out.println("[MEMENTO] Actualización completada correctamente.");

            return saved;

        } catch (Exception e) {
            // === REVERTIR SI FALLA ===
            if (caretaker.getHistorySize() > 0) {
                DegreeWorkMemento ultimoMemento = caretaker.getMemento(caretaker.getHistorySize() - 1);
                originator.restore(ultimoMemento);
                repository.save(originator.getDegreeWork());
                System.out.println("[MEMENTO] Error detectado. Estado anterior restaurado para el trabajo de grado con ID " + id);
            } else {
                System.out.println("[MEMENTO] No había un estado previo guardado para restaurar.");
            }

            throw e;
        }
    }


    /**
     * Método auxiliar para actualizar documentos
     */
    private void actualizarDocumentos(DegreeWorkDTO dto, DegreeWork existente) {

    // ============================
    // FORMATO A
    // ============================
    if (dto.getFormatosA() != null && !dto.getFormatosA().isEmpty()) {

        DocumentDTO formatoADto = dto.getFormatosA().get(0);
        Document formatoExistente = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.FORMATO_A);

        if (formatoExistente == null) {
            // Crear nuevo Formato A
            Document nuevoFormatoA = new Document();
            nuevoFormatoA.setTipo(EnumTipoDocumento.FORMATO_A);
            nuevoFormatoA.setRutaArchivo(formatoADto.getRutaArchivo());
            nuevoFormatoA.setEstado(formatoADto.getEstado());
            nuevoFormatoA.setFechaActual(LocalDate.now());

            existente.manejarRevision(nuevoFormatoA);

        } else {
            // Actualizar documento existente
            formatoExistente.setRutaArchivo(formatoADto.getRutaArchivo());
            formatoExistente.setEstado(formatoADto.getEstado());
            formatoExistente.setFechaActual(LocalDate.now());

            existente.manejarRevision(formatoExistente);
        }
    }

    // ============================
    // ANTEPROYECTO
    // ============================
    if (dto.getAnteproyectos() != null && !dto.getAnteproyectos().isEmpty()) {

        // Validar Formato A aceptado
        Document formatoA = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.FORMATO_A);
        if (formatoA == null || formatoA.getEstado() != EnumEstadoDocument.ACEPTADO) {
            throw new IllegalStateException("No se puede subir un anteproyecto hasta que el Formato A haya sido ACEPTADO.");
        }

        DocumentDTO anteDto = dto.getAnteproyectos().get(0);
        Document anteExistente = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.ANTEPROYECTO);

        if (anteExistente == null) {
            Document nuevo = new Document();
            nuevo.setTipo(EnumTipoDocumento.ANTEPROYECTO);
            nuevo.setRutaArchivo(anteDto.getRutaArchivo());
            nuevo.setEstado(anteDto.getEstado());
            nuevo.setFechaActual(LocalDate.now());
            existente.manejarRevision(nuevo);
        } else {
            anteExistente.setRutaArchivo(anteDto.getRutaArchivo());
            anteExistente.setEstado(anteDto.getEstado());
            anteExistente.setFechaActual(LocalDate.now());
            existente.manejarRevision(anteExistente);
        }
    }

    // ============================
    // CARTA DE ACEPTACIÓN
    // ============================
    if (dto.getCartasAceptacion() != null && !dto.getCartasAceptacion().isEmpty()) {

        DocumentDTO cartaDto = dto.getCartasAceptacion().get(0);

        Document cartaExistente = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.CARTA_ACEPTACION);

        if (cartaExistente == null) {
            Document nueva = new Document();
            nueva.setTipo(EnumTipoDocumento.CARTA_ACEPTACION);
            nueva.setRutaArchivo(cartaDto.getRutaArchivo());
            nueva.setEstado(cartaDto.getEstado());
            nueva.setFechaActual(LocalDate.now());
            existente.manejarRevision(nueva);
        } else {
            cartaExistente.setRutaArchivo(cartaDto.getRutaArchivo());
            cartaExistente.setEstado(cartaDto.getEstado());
            cartaExistente.setFechaActual(LocalDate.now());
            existente.manejarRevision(cartaExistente);
        }
    }
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
     * Listar trabajos por docente (director)
     */
    public List<DegreeWork> listarDegreeWorksPorDocente(String teacherEmail) {
        return repository.listByTeacher(teacherEmail);
    }

    /**
     * Listar trabajos por estudiante
     */
    public List<DegreeWork> listarDegreeWorksPorEstudiante(String studentEmail) {
        return repository.listByStudent(studentEmail);
    }

    /**
     * Listar anteproyectos
     */
    public List<DegreeWork> listarAnteproyectos(EnumEstadoDegreeWork estado) {
        return repository.findByEstado(estado);
    }

    /**
     * Eliminar un trabajo de grado
     */
    public void eliminarDegreeWork(Long id) {
        repository.deleteById(id);
    }

    /**
     * Actualizar correcciones y estado desde el microservicio de evaluaciones
    */
    @Transactional
    public void actualizarDesdeEvaluacion(DegreeWorkUpdateDTO dto) {
        if (dto == null || dto.getDegreeWorkId() == null) {
            throw new IllegalArgumentException("El DTO recibido desde Evaluaciones es inválido.");
        }

        System.out.println("📥 [RabbitMQ] Recibido mensaje de Evaluaciones: " + dto);

        Long id = dto.getDegreeWorkId().longValue();

        DegreeWork degreeWork = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el trabajo de grado con ID " + id));

        // --- Obtener el último documento subido (de cualquier tipo) ---
        Document ultimoDoc = null;
        if (degreeWork.getCartasAceptacion() != null && !degreeWork.getCartasAceptacion().isEmpty()) {
            ultimoDoc = degreeWork.getCartasAceptacion().get(degreeWork.getCartasAceptacion().size() - 1);
        } else if (degreeWork.getAnteproyectos() != null && !degreeWork.getAnteproyectos().isEmpty()) {
            ultimoDoc = degreeWork.getAnteproyectos().get(degreeWork.getAnteproyectos().size() - 1);
        } else if (degreeWork.getFormatosA() != null && !degreeWork.getFormatosA().isEmpty()) {
            ultimoDoc = degreeWork.getFormatosA().get(degreeWork.getFormatosA().size() - 1);
        }

        if (ultimoDoc == null) {
            // Si no hay documento, solo actualizamos correcciones si vienen (no hacer NPE)
            if (dto.getCorrecciones() != null && !dto.getCorrecciones().isBlank()) {
                degreeWork.setCorrecciones(dto.getCorrecciones());
                repository.save(degreeWork);
                System.out.println("[Evaluaciones] No había documentos. Guardadas correcciones para DegreeWork ID " + id);
                return;
            }
            throw new IllegalStateException("No se encontró ningún documento asociado al trabajo de grado.");
        }

        // Guardar estado previo del documento para logs / conteo
        EnumEstadoDocument estadoPrevio = ultimoDoc.getEstado();
        boolean cambioEstado = false;

        // --- Actualizar el estado del último documento (si viene estado) ---
        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            try {
                EnumEstadoDocument nuevoEstado = EnumEstadoDocument.valueOf(dto.getEstado().toUpperCase());
                if (nuevoEstado != ultimoDoc.getEstado()) {
                    ultimoDoc.setEstado(nuevoEstado);
                    ultimoDoc.setFechaActual(LocalDate.now());
                    cambioEstado = true;
                } else {
                    // Si es el mismo estado, solo actualizar fecha si quieres; por ahora dejamos como está
                }
            } catch (IllegalArgumentException e) {
                // Estado no reconocido: loguear y lanzar para que quien envía lo corrija
                throw new IllegalArgumentException("El estado recibido no es válido: " + dto.getEstado(), e);
            }
        }

        // --- Actualizar las correcciones (observaciones) si vienen ---
        boolean cambioCorrecciones = false;
        if (dto.getCorrecciones() != null) {
            String prevCorrecciones = degreeWork.getCorrecciones() == null ? "" : degreeWork.getCorrecciones();
            if (!dto.getCorrecciones().equals(prevCorrecciones)) {
                degreeWork.setCorrecciones(dto.getCorrecciones());
                cambioCorrecciones = true;
            }
        }

        // Si hubo cambios en el documento o en correcciones, guardar
        if (cambioEstado || cambioCorrecciones) {
            // IMPORTANTE: asegurarse de que la relación Owner esté bien y que JPA persista el documento
            // Si Document es entidad separada con repo, podrías guardar documentoRepository.save(ultimoDoc);
            repository.save(degreeWork);
            System.out.println("[Evaluaciones] DegreeWork ID " + id + " actualizado. Estado doc: " +
                    estadoPrevio + " -> " + ultimoDoc.getEstado() + ", correcciones actualizadas: " + cambioCorrecciones);
        } else {
            System.out.println("[Evaluaciones] DegreeWork ID " + id + " recibido pero sin cambios (estado/correcciones).");
        }

        // --- Si el estado fue distinto a ACEPTADO, incrementar contador (si corresponde) ---
        if (cambioEstado && !"ACEPTADO".equalsIgnoreCase(ultimoDoc.getEstado().name())) {
            degreeWork.setNoAprobadoCount(degreeWork.getNoAprobadoCount() + 1);
            repository.save(degreeWork);
            System.out.println("[Evaluaciones] Documento no aprobado. NoAprobadoCount incrementado a: " + degreeWork.getNoAprobadoCount());
        }
    }

}