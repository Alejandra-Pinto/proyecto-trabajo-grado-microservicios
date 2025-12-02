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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DegreeWorkService {

    private final DegreeWorkRepository repository;
    private final DocumentRepository documentRepository;
    private final DegreeWorkProducer degreeWorkProducer;

    public DegreeWorkService(DegreeWorkRepository repository,
            DocumentRepository documentRepository,
            DegreeWorkProducer degreeWorkProducer) {
        this.repository = repository;
        this.documentRepository = documentRepository;
        this.degreeWorkProducer = degreeWorkProducer;
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

            // 🔔 NOTIFICACIÓN DE EVALUACIÓN
            enviarNotificacion(
                    "EVALUACION_REALIZADA",
                    saved,
                    "El proyecto ha sido evaluado. Estado: " +
                            (dto.getEstado() != null ? dto.getEstado().name() : "sin cambio"));
        }

        return saved;
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

            degreeWorkProducer.sendUpdate(dto);

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
        degreeWorkProducer.sendUpdate(event);

        System.out.println("📤 [RABBITMQ] EvaluacionEventDTO enviado: " + event);

        DegreeWork saved = repository.save(degreeWork);
        // return repository.save(degreeWork);

        // 🔔 NOTIFICACIÓN
        enviarNotificacion(
                "EVALUADORES_ASIGNADOS",
                saved,
                "Se han asignado evaluadores al proyecto.");

        return saved;
    }

    /**
     * Enviar notificación a RabbitMQ (usado en asignar evaluadores y evaluar)
     */
    private void enviarNotificacion(String tipo, DegreeWork degreeWork, String mensaje) {
        try {
            NotificationEventDTO notification = new NotificationEventDTO(
                    tipo,
                    degreeWork.getTitulo() != null ? degreeWork.getTitulo() : null,
                    mensaje,
                    degreeWork.getEstudiantes().isEmpty() ? null : degreeWork.getEstudiantes().get(0).getEmail(),
                    degreeWork.getDirectorProyecto() != null ? degreeWork.getDirectorProyecto().getEmail() : null,
                    degreeWork.getCodirectoresProyecto().size() > 0
                            ? degreeWork.getCodirectoresProyecto().get(0).getEmail()
                            : null,
                    degreeWork.getCodirectoresProyecto().size() > 1
                            ? degreeWork.getCodirectoresProyecto().get(1).getEmail()
                            : null);

            degreeWorkProducer.sendNotification(notification);

            System.out.println("📨 [NOTIFICACIÓN] Enviada → " + notification);

        } catch (Exception e) {
            System.err.println("❌ Error enviando notificación: " + e.getMessage());
        }
    }

}
