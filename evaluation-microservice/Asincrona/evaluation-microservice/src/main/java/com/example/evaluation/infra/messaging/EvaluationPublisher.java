package com.example.evaluation.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.evaluation.infra.dto.DegreeWorkUpdateDTO;

@Component
public class EvaluationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.evaluation.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String routingKeyDegreeWork;

    public EvaluationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ✅ Solo publica el DTO con estado + correcciones
    public void publicarActualizacionDegreeWork(DegreeWorkUpdateDTO updateDTO) {
        try {
            System.out.println("[EvaluationPublisher] Enviando correcciones del DegreeWork...");
            System.out.println("   Exchange: " + exchange);
            System.out.println("   RoutingKey: " + routingKeyDegreeWork);
            System.out.println("   DegreeWork ID: " + updateDTO.getDegreeWorkId());
            System.out.println("   Estado: " + updateDTO.getEstado());
            System.out.println("   Correcciones: " + updateDTO.getCorrecciones());

            rabbitTemplate.convertAndSend(exchange, routingKeyDegreeWork, updateDTO);

            System.out.println("✅ [EvaluationPublisher] Correcciones publicadas correctamente en la cola.\n");
        } catch (Exception e) {
            System.err.println("❌ [EvaluationPublisher] Error al publicar correcciones: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
