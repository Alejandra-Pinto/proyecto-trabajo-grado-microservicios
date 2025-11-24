package com.example.evaluation.service;

import com.example.evaluation.entity.Document;
import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.Evaluation;
import com.example.evaluation.entity.enums.EnumEstadoDocument;
import com.example.evaluation.entity.enums.EnumTipoDocumento;
import com.example.evaluation.infra.dto.DegreeWorkUpdateDTO;
import com.example.evaluation.infra.dto.NotificationEventDTO;
import com.example.evaluation.infra.messaging.EvaluationPublisher;
import com.example.evaluation.infra.messaging.NotificationProducer;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.DocumentRepository;
import com.example.evaluation.repository.EvaluationRepository;
import com.example.evaluation.repository.EvaluadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class EvaluacionService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EvaluadorRepository evaluadorRepository;

    @Autowired
    private EvaluationPublisher evaluationPublisher;

    @Autowired
    private DegreeWorkRepository degreeWorkRepository;

    @Autowired
    private NotificationProducer notificationProducer;

    // ✅ Crear evaluación y enviar correcciones a la cola
    public Evaluation crearEvaluacion(
            Long documentId,
            String evaluadorCorreo,
            String resultado,
            String tipo,
            String correcciones) {

        System.out.println("🔍 [DEBUG] Iniciando crearEvaluacion...");
        System.out.println("🔍 [DEBUG] documentId: " + documentId + ", evaluador: " + evaluadorCorreo + ", resultado: " + resultado);

        // Buscar documento
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + documentId));
        System.out.println("🔍 [DEBUG] Documento encontrado: " + document.getId());

        // Buscar evaluador
        var evaluador = evaluadorRepository.findByCorreo(evaluadorCorreo)
                .orElseThrow(() -> new RuntimeException("Evaluador no encontrado con correo: " + evaluadorCorreo));
        System.out.println("🔍 [DEBUG] Evaluador encontrado: " + evaluador.getNombre());

        // Buscar DegreeWork que contiene el documento
        DegreeWork degreeWork = degreeWorkRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("No se encontró el trabajo de grado asociado al documento ID: " + documentId));
        System.out.println("🔍 [DEBUG] DegreeWork encontrado: " + degreeWork.getTitulo());

        // Crear evaluación
        Evaluation evaluacion = new Evaluation();
        evaluacion.setDocument(document);
        evaluacion.setEvaluador(evaluador);
        evaluacion.setResultado(resultado);
        evaluacion.setType(tipo);
        evaluacion.setSentAt(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(evaluacion);
        System.out.println("🔍 [DEBUG] Evaluación guardada con ID: " + saved.getId());

        // ✅ Obtener el estado actual del documento
        EnumEstadoDocument estadoDocumento = document.getEstado();

        // ✅ Actualizar calificación local y preparar DTO para degreework
        degreeWork.setCalificacion(resultado);
        degreeWork.setCorrecciones(correcciones);
        degreeWorkRepository.save(degreeWork);

        // ✅ Crear DTO para enviar por RabbitMQ (para actualizar degreework)
        DegreeWorkUpdateDTO updateDTO = DegreeWorkUpdateDTO.builder()
            .degreeWorkId(degreeWork.getId())
            .estado(estadoDocumento.name())
            .correcciones(correcciones)
            .calificacion(resultado)
            .build();

        // ✅ Publicar actualización a degreework
        evaluationPublisher.publicarActualizacionDegreeWork(updateDTO);

        // ✅ ENVIAR NOTIFICACIÓN (NUEVO)
        enviarNotificacionEvaluacion(degreeWork, document, resultado);

        return saved;
    }

    // ✅ Método para enviar notificaciones
    private void enviarNotificacionEvaluacion(DegreeWork degreeWork, Document document,
                                              String resultado) {
        try {
            // Determinar el tipo de evento basado en el resultado
            String eventType = determinarTipoEvento(resultado, document.getTipo());
            
            // Obtener emails de estudiantes (si existen)
            String studentEmail = null;
            if (degreeWork.getEstudiantesEmails() != null && !degreeWork.getEstudiantesEmails().isEmpty()) {
                studentEmail = degreeWork.getEstudiantesEmails().get(0);
            }

            // Obtener emails de codirectores (si existen)
            String coDirector1Email = null;
            String coDirector2Email = null;
            if (degreeWork.getCodirectoresEmails() != null && !degreeWork.getCodirectoresEmails().isEmpty()) {
                if (degreeWork.getCodirectoresEmails().size() >= 1) {
                    coDirector1Email = degreeWork.getCodirectoresEmails().get(0);
                }
                if (degreeWork.getCodirectoresEmails().size() >= 2) {
                    coDirector2Email = degreeWork.getCodirectoresEmails().get(1);
                }
            }

            // Obtener email del director
            String directorEmail = degreeWork.getDirectorEmail();

            // Crear evento de notificación
            NotificationEventDTO notificationEvent = new NotificationEventDTO(
                eventType,
                degreeWork.getTitulo(),
                degreeWork.getModalidad() != null ? degreeWork.getModalidad().name() : "NO_ESPECIFICADA",
                studentEmail, // Email del estudiante
                directorEmail, // Email del director
                coDirector1Email, // Primer codirector
                coDirector2Email  // Segundo codirector
            );

            // Enviar notificación a RabbitMQ
            notificationProducer.sendNotification(notificationEvent);

            // También puedes agregar logs para debugging
            System.out.println("📧 [Evaluaciones] Notificación enviada: " + eventType + 
                             " para el trabajo: " + degreeWork.getTitulo() +
                             " - Resultado: " + resultado);

        } catch (Exception e) {
            System.err.println("❌ [Evaluaciones] Error al enviar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Método auxiliar para determinar el tipo de evento
    private String determinarTipoEvento(String resultado, EnumTipoDocumento tipoDocumento) {
        String baseEvent = "";
        
        // Determinar el tipo de documento
        if (tipoDocumento != null) {
            switch (tipoDocumento) {
                case FORMATO_A:
                    baseEvent = "FORMATO_A_";
                    break;
                case ANTEPROYECTO:
                    baseEvent = "ANTEPROYECTO_";
                    break;
                case CARTA_ACEPTACION:
                    baseEvent = "CARTA_ACEPTACION_";
                    break;
                default:
                    baseEvent = "DOCUMENTO_";
            }
        } else {
            baseEvent = "DOCUMENTO_";
        }

        // Determinar el resultado
        if ("APROBADO".equalsIgnoreCase(resultado)) {
            return baseEvent + "APROBADO";
        } else if ("RECHAZADO".equalsIgnoreCase(resultado)) {
            return baseEvent + "RECHAZADO";
        } else if ("OBSERVADO".equalsIgnoreCase(resultado)) {
            return baseEvent + "OBSERVADO";
        } else {
            return baseEvent + "EVALUADO";
        }
    }

    // ✅ Listar todas las evaluaciones
    public List<Evaluation> listarEvaluaciones() {
        return evaluationRepository.findAll();
    }

    // ✅ Buscar evaluaciones por correo del evaluador
    public List<Evaluation> obtenerPorCorreoEvaluador(String correo) {
        var evaluador = evaluadorRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Evaluador no encontrado con correo: " + correo));
        return evaluationRepository.findByEvaluador(evaluador);
    }

    // ✅ Eliminar evaluación
    public void eliminarEvaluacion(Long id) {
        if (!evaluationRepository.existsById(id)) {
            throw new RuntimeException("Evaluación no encontrada con ID: " + id);
        }
        evaluationRepository.deleteById(id);
    }
}