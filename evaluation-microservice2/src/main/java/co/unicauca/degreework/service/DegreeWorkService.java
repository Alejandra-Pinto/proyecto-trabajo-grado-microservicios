package co.unicauca.degreework.service;

import co.unicauca.degreework.access.DegreeWorkRepository;
import co.unicauca.degreework.access.DocumentRepository;
import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.Document;
import co.unicauca.degreework.domain.entities.User;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import co.unicauca.degreework.infra.dto.ActualizarEvaluacionDTO;
import co.unicauca.degreework.infra.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.infra.dto.EvaluacionEventDTO;
import co.unicauca.degreework.infra.dto.NotificationEventDTO;
import co.unicauca.degreework.infra.messaging.DegreeWorkProducer;
import co.unicauca.degreework.infra.messaging.NotificationProducer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DegreeWorkService {

    private final DegreeWorkRepository repository;
    private final DocumentRepository documentRepository;
    private final DegreeWorkProducer degreeWorkProducer;
    private final NotificationProducer notificationProducer; // Agregar esto

    public DegreeWorkService(DegreeWorkRepository repository,
            DocumentRepository documentRepository,
            DegreeWorkProducer degreeWorkProducer,
            NotificationProducer notificationProducer) { // Agregar esto
        this.repository = repository;
        this.documentRepository = documentRepository;
        this.degreeWorkProducer = degreeWorkProducer;
        this.notificationProducer = notificationProducer; // Agregar esto
    }

    /**
     * Obtener un trabajo de grado por ID
     */
    public DegreeWork obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * Actualizar estado y observaciones y ENVIAR DegreeWorkUpdateDTO a la cola
     */
    @Transactional
    public DegreeWork actualizarEstadoYObservaciones(ActualizarEvaluacionDTO dto) {

        if (dto == null || dto.getDegreeWorkId() == null) {
            throw new IllegalArgumentException("DTO inválido: se requiere degreeWorkId.");
        }

        DegreeWork degreeWork = repository.findById(dto.getDegreeWorkId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un DegreeWork con ID " + dto.getDegreeWorkId()));

        // ---------------------------------------------------
        // OBTENER EL ÚLTIMO DOCUMENTO
        // ---------------------------------------------------
        Document ultimoDoc = obtenerUltimoDocumento(degreeWork);

        if (ultimoDoc == null) {
            throw new IllegalStateException("No hay documentos asociados al trabajo.");
        }

        EnumEstadoDocument estadoAnterior = ultimoDoc.getEstado();
        String obsPrevias = degreeWork.getCorrecciones();

        // ---------------------------------------------------
        // ACTUALIZAR ESTADO Y OBSERVACIONES
        // ---------------------------------------------------
        if (dto.getEstado() != null) {
            ultimoDoc.setEstado(dto.getEstado());
            ultimoDoc.setFechaActual(LocalDate.now());
        }

        if (dto.getObservaciones() != null) {
            degreeWork.setCorrecciones(dto.getObservaciones());
        }

        // -----------------------------
        // GUARDAR
        // -----------------------------
        DegreeWork saved = repository.save(degreeWork);

        boolean cambioEstado = dto.getEstado() != null &&
                !dto.getEstado().equals(estadoAnterior);

        boolean cambioObs = dto.getObservaciones() != null &&
                !dto.getObservaciones().equals(obsPrevias);

        // ---------------------------------------------------
        // ENVIAR A COLA SOLO SI HAY CAMBIOS
        // ---------------------------------------------------
        if (cambioEstado || cambioObs) {
            enviarDegreeWorkUpdate(saved, dto.getEstado(), dto.getObservaciones());

            // 🔔 NOTIFICACIÓN DE EVALUACIÓN - ACTUALIZADO
            // Determinar el tipo de documento evaluado
            String tipoDocumento = determinarTipoDocumento(ultimoDoc);
            Integer attemptNumber = null;
            
            if ("FORMATO_A".equals(tipoDocumento)) {
                // Calcular número de intento para Formato A
                attemptNumber = calcularNumeroIntentoFormatoA(degreeWork);
                
                // Enviar notificación específica para Formato A evaluado
                enviarNotificacion(
                        "FORMATO_A_EVALUADO",
                        saved,
                        "El Formato A ha sido evaluado",  // mensaje específico
                        dto.getEstado(),
                        dto.getObservaciones(),
                        attemptNumber);
            } else {
                // Para otros tipos de documentos
                enviarNotificacion(
                        "EVALUACION_REALIZADA",
                        saved,
                        "El documento ha sido evaluado",
                        dto.getEstado(),
                        dto.getObservaciones(),
                        null);
            }
        }

        return saved;
    }

    /**
     * Determinar el tipo de documento que se está evaluando
     */
    private String determinarTipoDocumento(Document documento) {
        if (documento == null) return "DESCONOCIDO";
        
        // Aquí necesitas tener acceso al tipo de documento en tu entidad Document
        // Si no tienes ese campo, necesitarás agregarlo
        return documento.getTipo() != null ? documento.getTipo().name() : "DESCONOCIDO";
    }

    /**
     * Calcular número de intento para Formato A
     */
    private Integer calcularNumeroIntentoFormatoA(DegreeWork degreeWork) {
        if (degreeWork.getFormatosA() == null || degreeWork.getFormatosA().isEmpty()) {
            return 1;
        }
        
        // Contar formatos A con estados que no sean rechazo definitivo
        long intentosValidos = degreeWork.getFormatosA().stream()
            .filter(doc -> doc.getEstado() != EnumEstadoDocument.RECHAZADO)
            .count();
            
        return (int) intentosValidos;
    }

    /**
     * Devuelve el último documento que tenga el DegreeWork (FormA, anteproyecto o
     * carta)
     */
    private Document obtenerUltimoDocumento(DegreeWork degreeWork) {

        if (degreeWork.getCartasAceptacion() != null && !degreeWork.getCartasAceptacion().isEmpty()) {
            return degreeWork.getCartasAceptacion()
                    .get(degreeWork.getCartasAceptacion().size() - 1);
        }

        if (degreeWork.getAnteproyectos() != null && !degreeWork.getAnteproyectos().isEmpty()) {
            return degreeWork.getAnteproyectos()
                    .get(degreeWork.getAnteproyectos().size() - 1);
        }

        if (degreeWork.getFormatosA() != null && !degreeWork.getFormatosA().isEmpty()) {
            return degreeWork.getFormatosA()
                    .get(degreeWork.getFormatosA().size() - 1);
        }

        return null;
    }

    /**
     * Enviar DegreeWorkUpdateDTO a RabbitMQ
     */
    private void enviarDegreeWorkUpdate(DegreeWork degreeWork,
            EnumEstadoDocument nuevoEstado,
            String observaciones) {
        try {
            DegreeWorkUpdateDTO dto = DegreeWorkUpdateDTO.builder()
                    .degreeWorkId(degreeWork.getId().intValue())
                    .estado(nuevoEstado != null ? nuevoEstado.name() : null)
                    .correcciones(observaciones)
                    .build();

            degreeWorkProducer.sendStatusUpdate(dto);

            System.out.println("📤 [RABBITMQ] DegreeWorkUpdateDTO enviado: " + dto);

        } catch (Exception e) {
            System.err.println("❌ Error enviando DegreeWorkUpdateDTO a la cola: " + e.getMessage());
        }
    }

    /**
     * Asignar evaluadores a un trabajo de grado y notificar al microservicio de
     * evaluación
     */
    public DegreeWork asignarEvaluadores(Long degreeWorkId, List<User> emailsEvaluadores) {
        // validación para que sean 2 evaluadores
        if (emailsEvaluadores.size() != 2) {
            throw new IllegalArgumentException("Debe asignar exactamente 2 evaluadores.");
        }

        // validación para ver que el trabajo de grado exista
        DegreeWork degreeWork = repository.findById(degreeWorkId)
                .orElseThrow(() -> new RuntimeException("Trabajo de grado no encontrado"));

        // Validación: cada evaluador NO debe tener más de 3 TG asignados
        for (User evaluador : emailsEvaluadores) {
            String email = evaluador.getEmail();

            int trabajosAsignados = repository.countByEvaluadorEmail(email);

            if (trabajosAsignados >= 3) {
                throw new IllegalArgumentException(
                        "El evaluador " + email + " ya tiene " + trabajosAsignados +
                                " trabajos asignados y no puede recibir más.");
            }
        }

        // Guardar evaluadores en la entidad
        List<String> correosEvaluadores = emailsEvaluadores.stream()
                .map(User::getEmail)
                .toList();

        // Guardar evaluadores en la entidad DegreeWork
        degreeWork.setEvaluadores(emailsEvaluadores);

        // Evento para evaluación (ya existente)
        EvaluacionEventDTO event = new EvaluacionEventDTO(degreeWorkId, correosEvaluadores);

        // Enviar a RabbitMQ
        degreeWorkProducer.sendEvaluatorsAssignment(event);

        System.out.println("📤 [RABBITMQ] EvaluacionEventDTO enviado: " + event);

        DegreeWork saved = repository.save(degreeWork);

        // 🔔 NOTIFICACIÓN ACTUALIZADA
        enviarNotificacion(
                "EVALUADORES_ASIGNADOS",
                saved,
                "Se han asignado evaluadores al anteproyecto",  // mensaje específico
                null,  // estado
                null,  // observaciones
                null   // attemptNumber
        );

        return saved;
    }

    /**
     * Enviar notificación a RabbitMQ (usado en asignar evaluadores y evaluar)
     */
    private void enviarNotificacion(String tipo, DegreeWork degreeWork, String mensaje, 
                                EnumEstadoDocument estado, String observaciones, Integer attemptNumber) {
        try {
            NotificationEventDTO notification = new NotificationEventDTO();
            
            // Establecer valores básicos
            notification.setEventType(tipo);
            notification.setTitle(degreeWork.getTitulo() != null ? degreeWork.getTitulo() : null);
            notification.setModality(degreeWork.getModalidad() != null ? degreeWork.getModalidad().name() : null);
            notification.setTimestamp(LocalDateTime.now());
            
            // Establecer estado y observaciones si están disponibles
            if (estado != null) {
                notification.setStatus(estado.name());
            }
            if (observaciones != null) {
                notification.setObservations(observaciones);
            }
            if (attemptNumber != null) {
                notification.setAttemptNumber(attemptNumber);
            }
            
            // Configurar destinatarios según el tipo de evento
            switch (tipo) {
                case "FORMATO_A_EVALUADO":
                    configurarDestinatariosFormatoAEvaluado(notification, degreeWork);
                    break;
                case "EVALUADORES_ASIGNADOS":
                    configurarDestinatariosEvaluadoresAsignados(notification, degreeWork);
                    break;
                default:
                    configurarDestinatariosGenerico(notification, degreeWork, mensaje);
            }
            
            // Usar NotificationProducer en lugar de DegreeWorkProducer
            notificationProducer.sendNotification(notification);

            System.out.println("📨 [NOTIFICACIÓN] Enviada → " + notification.getEventType() + 
                            " para trabajo: " + notification.getTitle());

        } catch (Exception e) {
            System.err.println("❌ Error enviando notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configurar destinatarios para evento FORMATO_A_EVALUADO
     */
    private void configurarDestinatariosFormatoAEvaluado(NotificationEventDTO notification, DegreeWork degreeWork) {
        System.out.println("=== CONFIGURANDO DESTINATARIOS FORMATO_A_EVALUADO ===");
        
        List<String> recipients = new ArrayList<>();
        
        // Notificar al director
        if (degreeWork.getDirectorProyecto() != null && degreeWork.getDirectorProyecto().getEmail() != null) {
            String directorEmail = degreeWork.getDirectorProyecto().getEmail().trim();
            recipients.add(directorEmail);
            notification.setDirectorEmail(directorEmail);
            System.out.println("✅ Director agregado: " + directorEmail);
        } else {
            System.out.println("⚠️ Director es null o no tiene email");
        }
        
        // Notificar a codirectores
        if (degreeWork.getCodirectoresProyecto() != null && !degreeWork.getCodirectoresProyecto().isEmpty()) {
            for (int i = 0; i < degreeWork.getCodirectoresProyecto().size(); i++) {
                User codirector = degreeWork.getCodirectoresProyecto().get(i);
                if (codirector != null && codirector.getEmail() != null) {
                    String codirectorEmail = codirector.getEmail().trim();
                    recipients.add(codirectorEmail);
                    if (i == 0) {
                        notification.setCoDirector1Email(codirectorEmail);
                    } else if (i == 1) {
                        notification.setCoDirector2Email(codirectorEmail);
                    }
                    System.out.println("✅ Codirector " + (i+1) + " agregado: " + codirectorEmail);
                }
            }
        } else {
            System.out.println("⚠️ Codirectores vacíos o null");
        }
        
        // Notificar a estudiantes si es necesario (opcional)
        if (degreeWork.getEstudiantes() != null && !degreeWork.getEstudiantes().isEmpty()) {
            List<String> studentEmails = degreeWork.getEstudiantes().stream()
                .filter(est -> est != null && est.getEmail() != null)
                .map(est -> est.getEmail().trim())
                .collect(Collectors.toList());
            
            if (!studentEmails.isEmpty()) {
                System.out.println("📚 Estudiantes encontrados: " + studentEmails);
                recipients.addAll(studentEmails);
            }
        }
        
        notification.setRecipientEmails(recipients);
        System.out.println("📧 Destinatarios finales: " + recipients);
        System.out.println("======================================================");
    }

    /**
     * Configurar destinatarios para evento EVALUADORES_ASIGNADOS
     */
    private void configurarDestinatariosEvaluadoresAsignados(NotificationEventDTO notification, DegreeWork degreeWork) {
        System.out.println("=== CONFIGURANDO DESTINATARIOS EVALUADORES_ASIGNADOS ===");
        
        List<String> recipients = new ArrayList<>();
        
        // Notificar a los evaluadores asignados
        if (degreeWork.getEvaluadores() != null && !degreeWork.getEvaluadores().isEmpty()) {
            List<String> evaluatorEmails = degreeWork.getEvaluadores().stream()
                .filter(eval -> eval != null && eval.getEmail() != null)
                .map(eval -> eval.getEmail().trim())
                .collect(Collectors.toList());
            
            recipients.addAll(evaluatorEmails);
            notification.setEvaluatorEmails(evaluatorEmails);
            
            System.out.println("✅ Evaluadores a notificar: " + evaluatorEmails);
        } else {
            System.out.println("⚠️ No hay evaluadores asignados");
        }
        
        // También notificar al director
        if (degreeWork.getDirectorProyecto() != null && degreeWork.getDirectorProyecto().getEmail() != null) {
            String directorEmail = degreeWork.getDirectorProyecto().getEmail().trim();
            if (!recipients.contains(directorEmail)) {
                recipients.add(directorEmail);
            }
            notification.setDirectorEmail(directorEmail);
            System.out.println("✅ Director agregado: " + directorEmail);
        }
        
        notification.setRecipientEmails(recipients);
        System.out.println("📧 Destinatarios finales: " + recipients);
        System.out.println("==========================================================");
    }
    /**
     * Configurar destinatarios genéricos (para otros tipos de eventos)
     */
    private void configurarDestinatariosGenerico(NotificationEventDTO notification, DegreeWork degreeWork, String mensaje) {
        List<String> recipients = new ArrayList<>();
        
        // Agregar director
        if (degreeWork.getDirectorProyecto() != null) {
            recipients.add(degreeWork.getDirectorProyecto().getEmail());
            notification.setDirectorEmail(degreeWork.getDirectorProyecto().getEmail());
        }
        
        notification.setRecipientEmails(recipients);
    }

}
