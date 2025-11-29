package co.unicauca.degreework.infra.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.unicauca.degreework.infra.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.service.DegreeWorkService;

@Component
public class DegreeWorkEvaluationListener {

    private final DegreeWorkService degreeWorkService;

    public DegreeWorkEvaluationListener(DegreeWorkService degreeWorkService) {
        this.degreeWorkService = degreeWorkService;
    }

    @RabbitListener(queues = "evaluation.queue")
    public void onUpdate(DegreeWorkUpdateDTO dto) {
        System.out.println("📥 Recibido UPDATE DTO: " + dto);
        degreeWorkService.actualizarDesdeEvaluacion(dto);
    }

    @RabbitListener(queues = "evaluation.queue")
    public void onEvaluadores(EvaluacionEventDTO dto) {
        System.out.println("📥 Recibido EVALUADORES DTO: " + dto);
        degreeWorkService.asignarEvaluadoresDesdeEvento(dto);
    }
}
