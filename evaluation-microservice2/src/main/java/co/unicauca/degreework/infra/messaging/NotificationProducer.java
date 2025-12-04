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
        try {
            System.out.println("=== ENVIANDO NOTIFICACIÓN DESDE EVALUATION ===");
            System.out.println("Exchange: " + exchange);
            System.out.println("Routing Key: " + routingKey);
            System.out.println("Event Type: " + event.getEventType());
            System.out.println("Title: " + event.getTitle());
            
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            
            System.out.println("✅ Notificación enviada correctamente a RabbitMQ");
            System.out.println("================================================");
        } catch (Exception e) {
            System.err.println("❌ Error enviando notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
