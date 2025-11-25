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
import co.unicauca.degreework.infra.dto.EvaluacionEventDTO;
import co.unicauca.degreework.infra.messaging.DegreeWorkProducer;
import co.unicauca.degreework.infra.messaging.NotificationProducer;
import co.unicauca.degreework.infra.dto.ActualizarEvaluacionDTO;

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
            throw new IllegalStateException("No se encontró ningún documento asociado al trabajo de grado.");
        }

        // --- Actualizar el estado del último documento ---
        try {
            EnumEstadoDocument nuevoEstado = EnumEstadoDocument.valueOf(dto.getEstado().toUpperCase());
            ultimoDoc.setEstado(nuevoEstado);
            ultimoDoc.setFechaActual(LocalDate.now());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El estado recibido no es válido: " + dto.getEstado());
        }

        // --- Actualizar las correcciones ---
        degreeWork.setCorrecciones(dto.getCorrecciones());

        // --- Si el estado no fue aceptado, incrementar contador ---
        if (!"ACEPTADO".equalsIgnoreCase(dto.getEstado())) {
            degreeWork.setNoAprobadoCount(degreeWork.getNoAprobadoCount() + 1);
            System.out.println("El documento fue rechazado o no aprobado. Se incrementa contador a: "
                    + degreeWork.getNoAprobadoCount());
        } else {
            System.out.println("Documento aceptado. No se incrementa contador.");
        }

        // --- Guardar cambios ---
        repository.save(degreeWork);

        System.out.println("[Evaluaciones] Estado del último documento y correcciones actualizados para DegreeWork ID " + id);
    }

    /**
     * Actualiza solo el estado y las observaciones de un trabajo de grado
     * Para uso específico de evaluadores - Y ENVÍA A COLA
     */
    @Transactional
    public DegreeWork actualizarEstadoYObservaciones(ActualizarEvaluacionDTO dto) {
        if (dto == null || dto.getDegreeWorkId() == null) {
            throw new IllegalArgumentException("DTO inválido: se requiere degreeWorkId");
        }
        
        DegreeWork degreeWork = repository.findById(dto.getDegreeWorkId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el trabajo de grado con ID " + dto.getDegreeWorkId()));
        
        // Guardar estado anterior para el evento
        EnumEstadoDegreeWork estadoAnterior = degreeWork.getEstado();
        String observacionesAnteriores = degreeWork.getCorrecciones();
        
        // Actualizar solo observaciones y estado
        if (dto.getObservaciones() != null) {
            degreeWork.setCorrecciones(dto.getObservaciones());
        }
        if (dto.getEstado() != null) {
            degreeWork.setEstado(dto.getEstado());
        }
        
        DegreeWork saved = repository.save(degreeWork);
        
        System.out.println("[EVALUACION] Trabajo de grado ID " + dto.getDegreeWorkId() + 
                        " actualizado - Estado: " + estadoAnterior + " → " + dto.getEstado());
        
        // ✅ ENVIAR EVALUACIÓN A LA COLA - SOLO SI HUBO CAMBIOS
        if (dto.getEstado() != null && !dto.getEstado().equals(estadoAnterior) || 
            (dto.getObservaciones() != null && !dto.getObservaciones().equals(observacionesAnteriores))) {
            
            enviarEvaluacionACola(saved, estadoAnterior, dto.getEstado(), dto.getObservaciones());
        }
        
        return saved;
    }

    /**
     * Método para enviar la evaluación a la cola de mensajes
     */
    private void enviarEvaluacionACola(DegreeWork degreeWork, EnumEstadoDegreeWork estadoAnterior, 
                                    EnumEstadoDegreeWork estadoNuevo, String observaciones) {
        try {
            EvaluacionEventDTO evento = new EvaluacionEventDTO(
                degreeWork.getId(),
                degreeWork.getTitulo(),
                observaciones,
                estadoAnterior,
                estadoNuevo
            );
            
            // Usar el producer existente o crear uno específico para evaluaciones
            degreeWorkProducer.sendEvaluacionEvent(evento);
            
            System.out.println("📤 [COLA] Evaluación enviada a la cola para DegreeWork ID: " + degreeWork.getId());
            
        } catch (Exception e) {
            System.err.println("❌ [COLA] Error enviando evaluación a la cola: " + e.getMessage());
            // No lanzamos excepción para no afectar la operación principal
        }
    }

}

YA FUNCIONAAAAAAAAAAAAAAAA