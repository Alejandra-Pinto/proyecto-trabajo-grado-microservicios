package com.example.evaluation.infra.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.evaluation.infra.dto.DegreeWorkAssignmentDTO;

@Component
public class DegreeWorkPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.evaluation.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String routingKey;

    public DegreeWorkPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarAsignacionEvaluadores(DegreeWorkAssignmentDTO asignacion) {
        try {
            System.out.println("📤 [DegreeWorkPublisher] Enviando asignación a DEGREEWORK SERVICE...");
            System.out.println("   Exchange: " + exchange);
            System.out.println("   RoutingKey: " + routingKey);
            System.out.println("   Queue destino: degreework.queue");
            System.out.println("   Trabajo de Grado ID: " + asignacion.getDegreeWorkId());
            System.out.println("   Título: " + asignacion.getTitulo());
            System.out.println("   Evaluador 1: " + asignacion.getNombreEvaluador1() + " ("
                    + asignacion.getCorreoEvaluador1() + ")");
            System.out.println("   Evaluador 2: " + asignacion.getNombreEvaluador2() + " ("
                    + asignacion.getCorreoEvaluador2() + ")");
            System.out.println("   Estado: " + asignacion.getEstado());

            rabbitTemplate.convertAndSend(exchange, routingKey, asignacion);

            System.out.println("✅ [DegreeWorkPublisher] Asignación publicada correctamente en degreework.queue\n");
        } catch (Exception e) {
            System.err.println("❌ [DegreeWorkPublisher] Error al publicar asignación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}