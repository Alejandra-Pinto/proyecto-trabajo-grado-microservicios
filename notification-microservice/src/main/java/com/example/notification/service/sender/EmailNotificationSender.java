package com.example.notification.service.sender;

import com.example.notification.infra.config.dto.NotificationEventDTO;
import com.example.notification.util.EmailSimulator;

public class EmailNotificationSender implements NotificationSender {

    private final EmailSimulator emailSimulator;

    public EmailNotificationSender(EmailSimulator emailSimulator) {
        this.emailSimulator = emailSimulator;
    }

    @Override
    public void send(NotificationEventDTO dto, String recipientEmail) {
        String subject = generateSubject(dto);
        String body = generateBody(dto);
        emailSimulator.simulate(recipientEmail, subject, body);
    }
    
    private String generateSubject(NotificationEventDTO dto) {
        // Puedes personalizar según el tipo de evento
        return "Notificación - " + dto.getEventType().replace("_", " ");
    }
    
    private String generateBody(NotificationEventDTO dto) {
        StringBuilder body = new StringBuilder();
        body.append("Trabajo de grado: ").append(dto.getTitle()).append("\n");
        body.append("Tipo de evento: ").append(dto.getEventType()).append("\n");
        
        if (dto.getModality() != null) {
            body.append("Modalidad: ").append(dto.getModality()).append("\n");
        }
        
        if (dto.getStatus() != null) {
            body.append("Estado: ").append(dto.getStatus()).append("\n");
        }
        
        if (dto.getAttemptNumber() != null) {
            body.append("Intento: ").append(dto.getAttemptNumber()).append("\n");
        }
        
        if (dto.getObservations() != null) {
            body.append("Observaciones: ").append(dto.getObservations()).append("\n");
        }
        
        body.append("Fecha: ").append(dto.getTimestamp()).append("\n");
        
        return body.toString();
    }
}