package com.example.notification.service.sender;

import com.example.notification.infra.config.dto.NotificationEventDTO;

public class InternalNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationEventDTO dto, String recipientEmail) {
        System.out.println("Notificación interna registrada para: "
                + dto.getDirectorEmail() + " - " + dto.getEventType());
    }
}
