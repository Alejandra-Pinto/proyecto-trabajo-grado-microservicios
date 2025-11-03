package com.example.evaluation.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.evaluation.infra.dto.EvaluationRequestDTO;

@Component
public class EvaluationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.user.routingkey}")
    private String routingKey;

    public EvaluationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEvaluacion(EvaluationRequestDTO evaluacion) {
        try {
            System.out.println("📤 [EvaluationPublisher] Enviando evaluación a USER SERVICE...");
            System.out.println("   Exchange: " + exchange);
            System.out.println("   RoutingKey: " + routingKey);
            System.out.println("   Queue destino: user.queue");
            System.out.println("   Evaluador: " + evaluacion.getEvaluadorCorreo());
            System.out.println("   Documento ID: " + evaluacion.getDocumentId());
            System.out.println("   Resultado: " + evaluacion.getResultado());
            System.out.println("   Tipo: " + evaluacion.getTipo());

            rabbitTemplate.convertAndSend(exchange, routingKey, evaluacion);

            System.out.println("✅ [EvaluationPublisher] Evaluación publicada correctamente en user.queue\n");
        } catch (Exception e) {
            System.err.println("❌ [EvaluationPublisher] Error al publicar evaluación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}