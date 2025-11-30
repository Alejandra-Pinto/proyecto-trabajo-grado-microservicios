package co.unicauca.degreework.hexagonal.adapter.out.messaging;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.hexagonal.application.dto.NotificationEventDTO;
import co.unicauca.degreework.hexagonal.port.out.messaging.EventPublisherPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.degreework.exchange}")
    private String degreeWorkExchange;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String degreeWorkRoutingKey;

    @Value("${app.rabbitmq.notification.exchange}")
    private String notificationExchange;

    @Value("${app.rabbitmq.notification.routingkey}")
    private String notificationRoutingKey;

    public EventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendDegreeWorkCreated(DegreeWorkCreatedEvent event) {
        System.out.println("Enviando evento DegreeWorkCreatedEvent a RabbitMQ...");
        rabbitTemplate.convertAndSend(degreeWorkExchange, degreeWorkRoutingKey, event);
        System.out.println("Evento enviado correctamente: " + event.getTitulo());
    }

    @Override
    public void sendNotification(NotificationEventDTO event) {
        System.out.println("📢 Enviando evento de notificación a RabbitMQ...");
        rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, event);
        System.out.println("✅ Notificación enviada correctamente: " + event.getEventType());
    }
}