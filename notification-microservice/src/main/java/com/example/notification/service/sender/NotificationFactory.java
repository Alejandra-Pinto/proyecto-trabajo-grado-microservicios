package com.example.notification.service.sender;

import com.example.notification.util.EmailSimulator;

public class NotificationFactory {

    public static NotificationSender create(String eventType, EmailSimulator emailSimulator) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de evento no puede ser nulo");
        }

        
        if (eventType.contains("SUBIDO") || eventType.contains("EVALUADO")) {
            return new EmailNotificationSender(emailSimulator);
        }

        // Ejemplo: notificaciones internas del sistema
        if (eventType.contains("INTERNA")) {
            return new InternalNotificationSender();
        }

        throw new IllegalArgumentException("Tipo de evento no soportado: " + eventType);
    }
}
