package com.example.evaluation.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.evaluation.infra.dto.NotificationEventDTO;

@Component
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.notification.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.notification.routingkey}")
    private String routingKey;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(NotificationEventDTO event) {
        try {
            System.out.println("📢 [Evaluaciones] Enviando evento de notificación a RabbitMQ...");
            System.out.println("🔍 [DEBUG] Exchange: " + exchange);
            System.out.println("🔍 [DEBUG] Routing Key: " + routingKey);
            System.out.println("🔍 [DEBUG] Evento: " + event.toString());
            
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            
            System.out.println("✅ [Evaluaciones] Notificación enviada correctamente: " + event.getEventType());
        } catch (Exception e) {
            System.err.println("❌ [Evaluaciones] Error enviando notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}