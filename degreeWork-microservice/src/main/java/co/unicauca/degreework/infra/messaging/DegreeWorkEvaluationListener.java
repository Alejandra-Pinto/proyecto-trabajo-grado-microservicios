package co.unicauca.degreework.infra.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import co.unicauca.degreework.infra.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.service.DegreeWorkService;

public class DegreeWorkEvaluationListener {

    private final DegreeWorkService degreeWorkService;

    public DegreeWorkEvaluationListener(DegreeWorkService degreeWorkService) {
        this.degreeWorkService = degreeWorkService;
    }

    @RabbitListener(queues = "evaluation.queue")
    public void onDegreeWorkUpdate(DegreeWorkUpdateDTO dto) {
        System.out.println("📥 [RabbitMQ] Recibido mensaje de Evaluaciones: " + dto);
        degreeWorkService.actualizarDesdeEvaluacion(dto);
    }
}
