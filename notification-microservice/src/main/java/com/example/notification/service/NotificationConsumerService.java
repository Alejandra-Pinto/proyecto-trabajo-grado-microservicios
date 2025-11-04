package com.example.notification.service;

import com.example.notification.infra.config.dto.*;
import com.example.notification.entity.Notification;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.util.EmailSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class NotificationConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumerService.class);
    private final NotificationRepository notificationRepo;
    private final EmailSimulator emailSimulator;

    public NotificationConsumerService(NotificationRepository notificationRepo, EmailSimulator emailSimulator) {
        this.notificationRepo = notificationRepo;
        this.emailSimulator = emailSimulator;
    }

    // Escucha mensajes que llegan a la cola de notificaciones
    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveEvent(NotificationEventDTO event) {
        logger.info("Evento recibido de tipo: {}", event.getEventType());

        switch (event.getEventType()) {
            case "TRABAJO_GRADO_REGISTRADO" -> handleDegreeWorkRegistered(event);
            case "FORMATO_A_SUBIDO" -> handleFormatoASubido(event);
            case "ANTEPROYECTO_SUBIDO" -> handleAnteproyectoSubido(event);
            case "MONOGRAFIA_SUBIDA" -> handleMonografiaSubida(event);
            default -> logger.warn("Tipo de evento desconocido: {}", event.getEventType());
        }
    }

    private void handleDegreeWorkRegistered(NotificationEventDTO event) {
        logger.info("Procesando evento TRABAJO_GRADO_REGISTRADO para {}", event.getTitle());

        // Notificar a estudiantes, director, codirectores
        sendIfPresent(event.getRecipientEmail(), "Tu trabajo de grado ha sido registrado correctamente.");
        sendIfPresent(event.getDirectorEmail(), "Eres director del nuevo trabajo de grado: " + event.getTitle());
        sendIfPresent(event.getCoDirector1Email(), "Eres codirector del trabajo de grado: " + event.getTitle());
        sendIfPresent(event.getCoDirector2Email(), "Eres segundo codirector del trabajo de grado: " + event.getTitle());

        // Notificación institucional
        sendInstitutionalNotification(
                "jefe@facultad.edu.co",
                "Nuevo trabajo de grado registrado",
                "Se ha registrado el trabajo " + event.getTitle() + " en modalidad " + event.getModality() + "."
        );
    }

    private void handleFormatoASubido(NotificationEventDTO event) {
        sendInstitutionalNotification(
                "coordinador@facultad.edu.co",
                "Nuevo Formato A pendiente a evaluar",
                "El proyecto '" + event.getTitle() + "' requiere evaluación en Formato A."
        );
    }

    private void handleAnteproyectoSubido(NotificationEventDTO event) {
        sendInstitutionalNotification(
                "jefe@departamento.edu.co",
                "Nuevo anteproyecto para revisión",
                "El anteproyecto '" + event.getTitle() + "' está listo para revisión."
        );
    }

    private void handleMonografiaSubida(NotificationEventDTO event) {
        sendInstitutionalNotification(
                "decanatura@facultad.edu.co",
                "Monografía para evaluación final",
                "Se ha recibido la monografía '" + event.getTitle() + "' para evaluación final."
        );
    }

    private void sendIfPresent(String email, String message) {
        if (email != null && !email.isBlank()) {
            saveAndSend(email, "Notificación automática", message);
        }
    }

    private void sendInstitutionalNotification(String email, String subject, String message) {
        saveAndSend(email, subject, message);
    }

    private void saveAndSend(String email, String subject, String message) {
        Notification n = new Notification();
        n.setRecipientEmail(email);
        n.setSubject(subject);
        n.setMessage(message);
        n.setSentAt(LocalDateTime.now());
        notificationRepo.save(n);
        emailSimulator.simulate(email, subject, message);
    }
}
