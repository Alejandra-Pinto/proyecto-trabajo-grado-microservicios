package co.unicauca.degreework.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.unicauca.degreework.infra.dto.EvaluacionEventDTO;
import co.unicauca.degreework.infra.dto.DegreeWorkCreatedEvent;

@Component
public class DegreeWorkProducer {

    private final RabbitTemplate rabbitTemplate;

    // Exchange general para eventos relacionados con trabajos de grado
    @Value("${app.rabbitmq.evaluation.exchange}")
    private String exchange;

    // Routing keys
    @Value("${app.rabbitmq.evaluation.routingkey}")
    private String routingKeyDegreeWorkCreated; // para eventos de creación

    @Value("${app.rabbitmq.evaluation.routingkey.update:}")
    private String routingKeyUpdate; // opcional, por si tienes uno diferente

    public DegreeWorkProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Evento cuando se crea un trabajo de grado
     */
    public void sendDegreeWorkCreated(DegreeWorkCreatedEvent event) {
        try {
            System.out.println("📤 Enviando evento DegreeWorkCreatedEvent a RabbitMQ...");
            rabbitTemplate.convertAndSend(exchange, routingKeyDegreeWorkCreated, event);
            System.out.println("✅ Evento enviado correctamente: " + event.getTitulo());
        } catch (Exception e) {
            System.err.println("❌ Error enviando evento DegreeWorkCreatedEvent: " + e.getMessage());
            throw new RuntimeException("Error enviando evento DegreeWorkCreatedEvent", e);
        }
    }

    /**
     * Evento cuando se registra una evaluación
     */
    public void sendEvaluacionEvent(EvaluacionEventDTO evento) {
        try {
            System.out.println("📤 Enviando EvaluacionEventDTO...");

            rabbitTemplate.convertAndSend(
                exchange,
                routingKeyDegreeWorkCreated, // si tienes otro routing key, cámbialo aquí
                evento
            );

            System.out.println("✅ Evento de evaluación enviado: " + evento.getDegreeWorkId());
        } catch (Exception e) {
            System.err.println("❌ Error enviando EvaluacionEventDTO: " + e.getMessage());
            throw new RuntimeException("Error enviando evento de evaluación", e);
        }
    }

    /**
     * Método opcional: enviar actualizaciones de trabajo o evaluación
     */
    public void sendUpdate(Object updateEvent) {
        try {
            System.out.println("📤 Enviando actualización a RabbitMQ...");

            String routingKeyToUse = (routingKeyUpdate == null || routingKeyUpdate.isEmpty())
                                      ? routingKeyDegreeWorkCreated
                                      : routingKeyUpdate;

            rabbitTemplate.convertAndSend(exchange, routingKeyToUse, updateEvent);

            System.out.println("✅ Actualización enviada correctamente: " + updateEvent.getClass().getSimpleName());
        } catch (Exception e) {
            System.err.println("❌ Error enviando actualización: " + e.getMessage());
            throw new RuntimeException("Error enviando evento de actualización", e);
        }
    }
}
