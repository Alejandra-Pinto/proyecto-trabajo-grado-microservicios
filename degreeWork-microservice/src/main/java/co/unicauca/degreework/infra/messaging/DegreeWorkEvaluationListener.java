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

    // Usar la property para que sea configurable y evitar hardcodear el nombre de la cola
    @RabbitListener(queues = "evaluation.queue")
    public void onDegreeWorkUpdate(DegreeWorkUpdateDTO dto) {
        System.out.println("📥 [RabbitMQ] Recibido mensaje de Evaluaciones: " + dto);
        try {
            degreeWorkService.actualizarDesdeEvaluacion(dto);
        } catch (Exception e) {
            // Log claro para debugging; no re-lanzamos aquí para evitar requeue infinito (si quieres otro comportamiento, ajustar)
            System.err.println("❌ Error procesando DegreeWorkUpdateDTO: " + e.getMessage());
            e.printStackTrace();
            throw e; // Spring manejará según la estrategia de error configurada (si prefieres evitar fatal, quita este throw)
        }
    }
}
