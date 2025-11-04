package com.example.notification.service.sender;

import com.example.notification.infra.config.dto.NotificationEventDTO;
import com.example.notification.util.EmailSimulator;

public class EmailNotificationSender implements NotificationSender {

    private final EmailSimulator emailSimulator;

    public EmailNotificationSender(EmailSimulator emailSimulator) {
        this.emailSimulator = emailSimulator;
    }

    @Override
    public void send(NotificationEventDTO dto) {
        String to = dto.getRecipientEmail() != null ? dto.getRecipientEmail() : dto.getCoordinatorEmail();
        String subject = "Notificación: " + dto.getEventType();
        String body = "Trabajo de grado: " + dto.getTitle()
                + "\nModalidad: " + dto.getModality()
                + "\nFecha: " + dto.getTimestamp();
        emailSimulator.simulate(to, subject, body);
    }
}
