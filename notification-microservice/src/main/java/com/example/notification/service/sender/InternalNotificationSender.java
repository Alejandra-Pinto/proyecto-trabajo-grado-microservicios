package com.example.notification.service.sender;

import com.example.notification.infra.config.dto.NotificationEventDTO;

public class InternalNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationEventDTO dto) {
        System.out.println("Notificación interna registrada para: "
                + dto.getCoordinatorEmail() + " - " + dto.getEventType());
    }
}
