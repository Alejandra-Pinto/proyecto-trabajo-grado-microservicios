package com.example.notification.service.sender;

import com.example.notification.infra.config.dto.NotificationEventDTO;

public interface NotificationSender {
    void send(NotificationEventDTO dto, String recipientEmail);
}