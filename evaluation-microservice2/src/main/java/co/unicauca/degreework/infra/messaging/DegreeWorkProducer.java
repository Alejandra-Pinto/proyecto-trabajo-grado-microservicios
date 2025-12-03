package co.unicauca.degreework.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DegreeWorkProducer {

    private final RabbitTemplate rabbitTemplate;

    // Exchange para evaluaciones
    @Value("${app.rabbitmq.evaluation.exchange}")
    private String exchange;

    // Colas específicas para diferentes tipos de mensajes
    @Value("${app.rabbitmq.evaluation.queue.status}")
    private String statusQueue;
    
    @Value("${app.rabbitmq.evaluation.queue.evaluators}")
    private String evaluatorsQueue;

    // Routing key para compatibilidad con versiones anteriores si es necesario
    @Value("${app.rabbitmq.evaluation.routingkey}")
    private String routingKey;

    public DegreeWorkProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Envía actualizaciones de estado/correcciones del trabajo de grado
     * Usa la cola específica para estados
     */
    public void sendStatusUpdate(Object statusUpdateEvent) {
        try {
            System.out.println("📤 Enviando ACTUALIZACIÓN DE ESTADO a RabbitMQ...");
            System.out.println("📋 Cola destino: " + statusQueue);
            System.out.println("📦 Tipo de mensaje: " + statusUpdateEvent.getClass().getSimpleName());

            // Envía directamente a la cola (exchange directo vacío "")
            rabbitTemplate.convertAndSend("", statusQueue, statusUpdateEvent);

            System.out.println("✅ Actualización de estado enviada correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error enviando actualización de estado: " + e.getMessage());
            throw new RuntimeException("Error enviando evento de actualización de estado", e);
        }
    }

    /**
     * Envía asignación de evaluadores
     * Usa la cola específica para evaluadores
     */
    public void sendEvaluatorsAssignment(Object evaluatorsEvent) {
        try {
            System.out.println("👥 Enviando ASIGNACIÓN DE EVALUADORES a RabbitMQ...");
            System.out.println("📋 Cola destino: " + evaluatorsQueue);
            System.out.println("📦 Tipo de mensaje: " + evaluatorsEvent.getClass().getSimpleName());

            // Envía directamente a la cola (exchange directo vacío "")
            rabbitTemplate.convertAndSend("", evaluatorsQueue, evaluatorsEvent);

            System.out.println("✅ Asignación de evaluadores enviada correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error enviando asignación de evaluadores: " + e.getMessage());
            throw new RuntimeException("Error enviando evento de asignación de evaluadores", e);
        }
    }

    /**
     * Método alternativo usando el exchange si lo prefieres
     * (Mantén solo si realmente necesitas el exchange)
     */
    public void sendStatusUpdateViaExchange(Object statusUpdateEvent) {
        try {
            System.out.println("📤 Enviando ACTUALIZACIÓN DE ESTADO vía Exchange...");
            System.out.println("🏪 Exchange: " + exchange);
            System.out.println("🔑 Routing key: " + routingKey);
            System.out.println("📦 Tipo de mensaje: " + statusUpdateEvent.getClass().getSimpleName());

            rabbitTemplate.convertAndSend(exchange, routingKey, statusUpdateEvent);

            System.out.println("✅ Actualización de estado enviada vía exchange");
        } catch (Exception e) {
            System.err.println("❌ Error enviando actualización vía exchange: " + e.getMessage());
            throw new RuntimeException("Error enviando evento vía exchange", e);
        }
    }

    public void sendNotification(Object notificationEvent) {
        try {
            System.out.println("📨 Enviando NOTIFICACIÓN a RabbitMQ...");

            rabbitTemplate.convertAndSend(exchange, routingKey, notificationEvent);

            System.out.println("✅ Notificación enviada correctamente: " + notificationEvent.getClass().getSimpleName());
        } catch (Exception e) {
            System.err.println("❌ Error enviando notificación: " + e.getMessage());
            throw new RuntimeException("Error enviando notificación", e);
        }
    }
}