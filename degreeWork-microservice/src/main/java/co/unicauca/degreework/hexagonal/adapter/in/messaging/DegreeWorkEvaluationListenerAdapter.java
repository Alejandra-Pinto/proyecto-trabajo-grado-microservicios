package co.unicauca.degreework.hexagonal.adapter.in.messaging;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.hexagonal.application.dto.EvaluacionEventDTO;
import co.unicauca.degreework.hexagonal.application.service.DegreeWorkEvaluationUseCase;
import co.unicauca.degreework.hexagonal.port.in.messaging.DegreeWorkEvaluationListenerPort;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// DegreeWorkEvaluationListenerAdapter.java
@Component
public class DegreeWorkEvaluationListenerAdapter implements DegreeWorkEvaluationListenerPort {

    private final DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase;

    public DegreeWorkEvaluationListenerAdapter(DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase) {
        this.degreeWorkEvaluationUseCase = degreeWorkEvaluationUseCase;
    }

    @Override
    @RabbitListener(queues = "evaluation.queue")
    public void onUpdate(DegreeWorkUpdateDTO dto) {
        System.out.println("📥 Recibido UPDATE DTO: " + dto);
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);
    }

    @Override
    @RabbitListener(queues = "evaluation.queue")
    public void onEvaluadores(EvaluacionEventDTO dto) {
        System.out.println("📥 Recibido EVALUADORES DTO: " + dto);
        degreeWorkEvaluationUseCase.asignarEvaluadores(dto);
    }
}