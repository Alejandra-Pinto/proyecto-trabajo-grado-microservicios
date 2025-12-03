package co.unicauca.degreework.hexagonal.adapter.in.messaging;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.hexagonal.application.dto.EvaluacionEventDTO;
import co.unicauca.degreework.hexagonal.application.service.DegreeWorkEvaluationUseCase;
import co.unicauca.degreework.hexagonal.port.in.messaging.DegreeWorkEvaluationListenerPort;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DegreeWorkEvaluationListenerAdapter implements DegreeWorkEvaluationListenerPort {

    private final DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase;
    private final ObjectMapper objectMapper;
    
    // Nombres de colas desde properties
    @Value("${app.rabbitmq.evaluation.queue.status}")
    private String statusQueue;
    
    @Value("${app.rabbitmq.evaluation.queue.evaluators}")
    private String evaluatorsQueue;
    
    public DegreeWorkEvaluationListenerAdapter(
            DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase,
            ObjectMapper objectMapper) {
        this.degreeWorkEvaluationUseCase = degreeWorkEvaluationUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    @RabbitListener(queues = "${app.rabbitmq.evaluation.queue.status}")
    public void onUpdate(DegreeWorkUpdateDTO dto) {
        System.out.println("📥 [Estado] Recibido mensaje en cola: " + statusQueue);
        System.out.println("📦 DTO recibido: " + dto.getClass().getSimpleName());
        
        // Validación básica
        if (dto == null) {
            System.err.println("❌ [Estado] DTO es null, ignorando mensaje");
            return;
        }
        
        System.out.println("🆔 ID del trabajo de grado: " + dto.getDegreeWorkId());
        System.out.println("📊 Estado: " + dto.getEstado());
        System.out.println("📝 Correcciones: " + dto.getCorrecciones());
        
        try {
            degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);
            System.out.println("✅ [Estado] Procesado exitosamente");
        } catch (Exception e) {
            System.err.println("❌ [Estado] Error procesando el mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @RabbitListener(queues = "${app.rabbitmq.evaluation.queue.evaluators}")
    public void onEvaluadores(EvaluacionEventDTO dto) {
        System.out.println("📥 [Evaluadores] Recibido mensaje en cola: " + evaluatorsQueue);
        System.out.println("📦 DTO recibido: " + dto.getClass().getSimpleName());
        
        // Validación básica
        if (dto == null) {
            System.err.println("❌ [Evaluadores] DTO es null, ignorando mensaje");
            return;
        }
        
        System.out.println("🆔 ID del trabajo de grado: " + dto.getDegreeWorkId());
        System.out.println("👥 Número de evaluadores: " + 
            (dto.getEvaluadores() != null ? dto.getEvaluadores().size() : 0));
        
        try {
            degreeWorkEvaluationUseCase.asignarEvaluadores(dto);
            System.out.println("✅ [Evaluadores] Procesado exitosamente");
        } catch (Exception e) {
            System.err.println("❌ [Evaluadores] Error procesando el mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}