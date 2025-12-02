package com.example.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.entity.Notification;
import com.example.notification.infra.config.dto.NotificationEventDTO;
import com.example.notification.service.sender.*;
import com.example.notification.util.EmailSimulator;
import java.util.List;

@Service
public class NotificationConsumerService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailSimulator emailSimulator;
    
    @Autowired
    private RecipientService recipientService;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
public void receive(NotificationEventDTO dto) {
    try {
        // DEPURACIÓN: Ver qué datos llegan
        System.out.println("=== EVENTO RECIBIDO ===");
        System.out.println("Event Type: " + dto.getEventType());
        System.out.println("Title: " + dto.getTitle());
        System.out.println("Target Role: " + dto.getTargetRole());
        System.out.println("Recipient Emails: " + (dto.getRecipientEmails() != null ? dto.getRecipientEmails() : "null"));
        System.out.println("Director Email: " + dto.getDirectorEmail());
        System.out.println("=========================");
        
        // 1️ Determinar destinatarios
        List<String> recipients = recipientService.resolveRecipients(dto);
        
        // DEPURACIÓN: Ver qué destinatarios se resolvieron
        System.out.println("Destinatarios resueltos: " + recipients.size());
        for (String recipient : recipients) {
            System.out.println("  - " + recipient);
        }
        
        if (recipients.isEmpty()) {
            System.err.println("WARN: No hay destinatarios para el evento: " + dto.getEventType());
            return;
        }

        // Resto del código...

            // 2️ Guardar en BD y enviar a cada destinatario
            for (String recipientEmail : recipients) {
                // Guardar en BD
                Notification entity = new Notification();
                entity.setRecipientEmail(recipientEmail);
                entity.setSubject(generateSubject(dto));
                entity.setMessage(generateMessage(dto));
                entity.setType(dto.getEventType());
                notificationRepository.save(entity);

                // Enviar notificación
                NotificationSender sender = NotificationFactory.create(dto.getEventType(), emailSimulator);
                sender.send(dto, recipientEmail);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String generateSubject(NotificationEventDTO dto) {
        switch (dto.getEventType()) {
            case "FORMATO_A_SUBIDO":
                return "Nuevo Formato A enviado - " + dto.getTitle();
            case "FORMATO_A_EVALUADO":
                return "Formato A evaluado - " + dto.getTitle();
            case "ANTEPROYECTO_SUBIDO":
                return "Nuevo Anteproyecto enviado - " + dto.getTitle();
            case "EVALUADORES_ASIGNADOS":
                return "Asignado como evaluador - " + dto.getTitle();
            default:
                return "Notificación del sistema - " + dto.getTitle();
        }
    }
    
    private String generateMessage(NotificationEventDTO dto) {
        StringBuilder message = new StringBuilder();
        message.append("Trabajo de grado: ").append(dto.getTitle()).append("\n");
        
        if (dto.getModality() != null) {
            message.append("Modalidad: ").append(dto.getModality()).append("\n");
        }
        
        if (dto.getAttemptNumber() != null) {
            message.append("Intento: ").append(dto.getAttemptNumber()).append("\n");
        }
        
        if (dto.getStatus() != null) {
            message.append("Estado: ").append(dto.getStatus()).append("\n");
        }
        
        if (dto.getObservations() != null) {
            message.append("Observaciones: ").append(dto.getObservations()).append("\n");
        }
        
        message.append("Fecha: ").append(dto.getTimestamp()).append("\n");
        
        return message.toString();
    }
}