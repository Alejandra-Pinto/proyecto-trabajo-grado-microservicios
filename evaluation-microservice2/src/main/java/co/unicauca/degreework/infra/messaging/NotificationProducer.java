package co.unicauca.degreework.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import co.unicauca.degreework.infra.dto.NotificationEventDTO;

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
        System.out.println("📢 Enviando evento de notificación a RabbitMQ...");
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        System.out.println("✅ Notificación enviada correctamente: " + event.getEventType());
    }
}
