package co.unicauca.degreework.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.unicauca.degreework.infra.dto.DegreeWorkCreatedEvent;

@Component
public class DegreeWorkProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.degreework.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String routingKey;

    public DegreeWorkProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDegreeWorkCreated(DegreeWorkCreatedEvent event) {
        System.out.println("Enviando evento DegreeWorkCreatedEvent a RabbitMQ...");
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        System.out.println("Evento enviado correctamente: " + event.getTitulo());
    }
}
