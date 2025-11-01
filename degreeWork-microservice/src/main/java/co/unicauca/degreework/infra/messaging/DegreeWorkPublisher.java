package co.unicauca.degreework.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DegreeWorkPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.degreework.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String routingKey;

    public DegreeWorkPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDegreeWorkChange(Object event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        System.out.println("Evento DegreeWork publicado: " + event);
    }
}
