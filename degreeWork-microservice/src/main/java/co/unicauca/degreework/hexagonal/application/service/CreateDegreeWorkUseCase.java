package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkDTO;
import co.unicauca.degreework.hexagonal.application.dto.NotificationEventDTO;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumModalidad;
import co.unicauca.degreework.hexagonal.domain.patterns.builder.DegreeWorkBuilder;
import co.unicauca.degreework.hexagonal.domain.patterns.builder.DegreeWorkDirector;
import co.unicauca.degreework.hexagonal.domain.patterns.builder.ProfessionalPracticeBuilder;
import co.unicauca.degreework.hexagonal.domain.patterns.builder.ResearchDegreeWorkBuilder;
import co.unicauca.degreework.hexagonal.domain.service.DegreeWorkValidationService;
import co.unicauca.degreework.hexagonal.infra.mapper.DegreeWorkEventMapper;
import co.unicauca.degreework.hexagonal.infra.mapper.DegreeWorkMapper;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.messaging.EventPublisherPort;
import co.unicauca.degreework.hexagonal.domain.vo.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class CreateDegreeWorkUseCase {

    private final DegreeWorkRepositoryPort degreeWorkRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final DegreeWorkValidationService validationService;
    private final DegreeWorkMapper degreeWorkMapper;
    private final DegreeWorkEventMapper degreeWorkEventMapper;

    @Autowired
    public CreateDegreeWorkUseCase(
            DegreeWorkRepositoryPort degreeWorkRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            EventPublisherPort eventPublisherPort,
            DegreeWorkValidationService validationService,
            DegreeWorkMapper degreeWorkMapper,
            DegreeWorkEventMapper degreeWorkEventMapper) {
        this.degreeWorkRepositoryPort = degreeWorkRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.validationService = validationService;
        this.degreeWorkMapper = degreeWorkMapper;
        this.degreeWorkEventMapper = degreeWorkEventMapper;
    }

    public DegreeWorkDTO execute(DegreeWorkDTO dto) {
        // 1. Validar y obtener usuarios
        User director = userRepositoryPort.findByEmail(dto.getDirectorEmail())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el director con correo: " + dto.getDirectorEmail()));

        List<User> estudiantes = validationService.validarYObternerEstudiantes(dto.getEstudiantesEmails(), userRepositoryPort);
        List<User> codirectores = validationService.validarYObternerCodirectores(dto.getCodirectoresEmails(), userRepositoryPort);

        // 2. Crear builder según modalidad
        DegreeWorkBuilder builder = crearBuilder(dto.getModalidad());

        // 3. Construir el DegreeWork usando el patrón Builder
        DegreeWorkDirector directorBuilder = new DegreeWorkDirector();
        directorBuilder.setBuilder(builder);

        // Usar el mapper para convertir los datos del DTO al builder
        // Nota: Los builders necesitarán ajustarse para aceptar Value Objects
        builder.titulo(Titulo.createTitulo(dto.getTitulo()))
                .director(director)
                .objetivoGeneral(dto.getObjetivoGeneral())
                .objetivosEspecificos(dto.getObjetivosEspecificos())
                .fechaActual(FechaCreacion.createFechaCreacion(dto.getFechaActual()))
                .estadoInicial(dto.getEstado());

        estudiantes.forEach(builder::agregarEstudiante);
        codirectores.forEach(builder::agregarCodirector);

        // Convertir documentos DTO a entidades Domain si es necesario
        // Esto dependerá de cómo esté implementado tu builder
        if (dto.getFormatosA() != null) {
            builder.documentosDesdeDTOs(dto.getFormatosA());
        }
        if (dto.getAnteproyectos() != null) {
            builder.documentosDesdeDTOs(dto.getAnteproyectos());
        }
        if (dto.getCartasAceptacion() != null) {
            builder.documentosDesdeDTOs(dto.getCartasAceptacion());
        }

        DegreeWork degreeWork = directorBuilder.construirTrabajo();

        // 4. Persistir
        DegreeWork saved = degreeWorkRepositoryPort.save(degreeWork);

        // 5. Publicar eventos usando los mappers
        publicarEventos(saved, director, estudiantes, codirectores);

        return degreeWorkMapper.toDTO(saved);
    }

    private DegreeWorkBuilder crearBuilder(EnumModalidad modalidad) {
        switch (modalidad) {
            case INVESTIGACION:
                return new ResearchDegreeWorkBuilder();
            case PRACTICA_PROFESIONAL:
                return new ProfessionalPracticeBuilder();
            default:
                throw new IllegalArgumentException("Modalidad no soportada: " + modalidad);
        }
    }

    private void publicarEventos(DegreeWork saved, User director, List<User> estudiantes, 
                               List<User> codirectores) {
        // Evento para evaluation microservice usando el mapper
        DegreeWorkCreatedEvent event = degreeWorkEventMapper.toCreatedEvent(saved);
        eventPublisherPort.sendDegreeWorkCreated(event);

        // Evento de notificación - ACTUALIZADO para usar nueva estructura
        NotificationEventDTO notificationEvent = createNotificationEvent(saved, director, estudiantes, codirectores);
        eventPublisherPort.sendNotification(notificationEvent);
    }

    private NotificationEventDTO createNotificationEvent(DegreeWork saved, User director, 
                                                        List<User> estudiantes, List<User> codirectores) {
        NotificationEventDTO dto = new NotificationEventDTO();
        
        // Establecer valores básicos
        dto.setEventType("FORMATO_A_SUBIDO");
        dto.setTitle(saved.getTitulo() != null ? saved.getTitulo().getValor() : null);
        dto.setModality(saved.getModalidad() != null ? saved.getModalidad().name() : null);
        dto.setTimestamp(LocalDateTime.now());
        
        // Establecer emails de los involucrados
        List<String> directorAndCodirectors = new ArrayList<>();
        directorAndCodirectors.add(director.getEmail());
        
        // Agregar codirectores si existen
        for (User codirector : codirectores) {
            directorAndCodirectors.add(codirector.getEmail());
        }
        dto.setRecipientEmails(directorAndCodirectors);
        
        // Para este evento, TODOS los coordinadores deben ser notificados
        dto.setTargetRole("COORDINATOR");
        
        // Establecer emails específicos para referencia (no para envío directo)
        dto.setDirectorEmail(director.getEmail());
        
        if (!codirectores.isEmpty()) {
            dto.setCoDirector1Email(codirectores.get(0).getEmail());
            if (codirectores.size() > 1) {
                dto.setCoDirector2Email(codirectores.get(1).getEmail());
            }
        }
        
        // Si hay estudiantes, agregarlos también
        if (!estudiantes.isEmpty()) {
            List<String> studentEmails = new ArrayList<>();
            for (User estudiante : estudiantes) {
                studentEmails.add(estudiante.getEmail());
            }
            // Para este evento, los estudiantes NO reciben notificación inmediata
            // Solo se notifican cuando el coordinador evalúe
        }
        
        return dto;
    }

}