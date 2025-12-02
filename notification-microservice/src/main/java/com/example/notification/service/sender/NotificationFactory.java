package com.example.notification.service.sender;

import com.example.notification.util.EmailSimulator;

public class NotificationFactory {

    public static NotificationSender create(String eventType, EmailSimulator emailSimulator) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de evento no puede ser nulo");
        }

        // Todos los eventos importantes van por email
        if (eventType.contains("FORMATO_A") || 
            eventType.contains("ANTEPROYECTO") || 
            eventType.contains("EVALUADORES") ||
            eventType.contains("TRABAJO_GRADO")) {
            return new EmailNotificationSender(emailSimulator);
        }

        // Para notificaciones internas
        if (eventType.contains("INTERNA")) {
            return new InternalNotificationSender();
        }

        // Por defecto email
        System.err.println("WARN: Evento no configurado específicamente: " + eventType + " → usando Email");
        return new EmailNotificationSender(emailSimulator);
    }
}