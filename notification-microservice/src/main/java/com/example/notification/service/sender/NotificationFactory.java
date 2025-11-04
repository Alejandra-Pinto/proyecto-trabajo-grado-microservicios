package com.example.notification.service.sender;

import com.example.notification.util.EmailSimulator;

public class NotificationFactory {

    public static NotificationSender create(String eventType, EmailSimulator emailSimulator) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de evento no puede ser nulo");
        }

        // 1. SUBIDO o EVALUADO → Email
        if (eventType.contains("SUBIDO") || eventType.contains("EVALUADO")) {
            return new EmailNotificationSender(emailSimulator);
        }

        // 2. TRABAJO_GRADO_REGISTRADO → también Email
        if (eventType.contains("TRABAJO_GRADO")) {
            return new EmailNotificationSender(emailSimulator);
        }

        // 3. Notificaciones internas
        if (eventType.contains("INTERNA")) {
            return new InternalNotificationSender();
        }

        // 4. Por defecto: Email (opcional, para evitar errores)
        System.err.println("WARN: Evento no configurado específicamente: " + eventType + " → usando Email");
        return new EmailNotificationSender(emailSimulator);

        // throw new IllegalArgumentException("Tipo de evento no soportado: " + eventType);
    }
}
