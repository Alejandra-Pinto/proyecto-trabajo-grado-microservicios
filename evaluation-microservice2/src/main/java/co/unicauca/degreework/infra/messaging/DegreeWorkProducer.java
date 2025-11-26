package co.unicauca.degreework.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import co.unicauca.degreework.infra.dto.EvaluacionEventDTO;

import co.unicauca.degreework.infra.dto.DegreeWorkCreatedEvent;

@Component
public class DegreeWorkProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.evaluation.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.evaluation.routingkey}")
    private String routingKey;

    public DegreeWorkProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDegreeWorkCreated(DegreeWorkCreatedEvent event) {
        System.out.println("Enviando evento DegreeWorkCreatedEvent a RabbitMQ...");
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        System.out.println("Evento enviado correctamente: " + event.getTitulo());
    }

    public void sendEvaluacionEvent(EvaluacionEventDTO evento) {
    try {
        rabbitTemplate.convertAndSend(
            exchange, // o el exchange que uses
            routingKey, // routing key específico para evaluaciones
            evento
        );
        System.out.println("✅ Evento de evaluación enviado a la cola: " + evento.getDegreeWorkId());
    } catch (Exception e) {
        System.err.println("❌ Error enviando evento de evaluación: " + e.getMessage());
        throw new RuntimeException("Error enviando evento de evaluación", e);
    }
}
}
