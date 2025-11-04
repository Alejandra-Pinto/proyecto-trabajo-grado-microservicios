package com.example.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.entity.Notification;
import com.example.notification.infra.config.dto.NotificationEventDTO;
import com.example.notification.service.sender.*;
import com.example.notification.util.EmailSimulator;

@Service
public class NotificationConsumerService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailSimulator emailSimulator;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receive(NotificationEventDTO dto) {
        try {
            // 1️ Guardar en la BD
            Notification entity = new Notification();
            entity.setRecipientEmail(dto.getRecipientEmail());
            entity.setSubject("Evento: " + dto.getEventType());
            entity.setMessage("Título: " + dto.getTitle() + "\nModalidad: " + dto.getModality());
            entity.setType(dto.getEventType());
            notificationRepository.save(entity);

            // 2️ Enviar usando la fábrica
            NotificationSender sender = NotificationFactory.create(dto.getEventType(), emailSimulator);
            sender.send(dto);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
