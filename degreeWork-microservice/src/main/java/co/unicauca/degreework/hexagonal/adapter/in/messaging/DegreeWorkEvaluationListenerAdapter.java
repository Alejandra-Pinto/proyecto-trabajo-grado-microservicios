package co.unicauca.degreework.hexagonal.adapter.in.messaging;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.hexagonal.application.dto.EvaluacionEventDTO;
import co.unicauca.degreework.hexagonal.application.service.DegreeWorkEvaluationUseCase;
import co.unicauca.degreework.hexagonal.port.in.messaging.DegreeWorkEvaluationListenerPort;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

// DegreeWorkEvaluationListenerAdapter.java
@Component
public class DegreeWorkEvaluationListenerAdapter implements DegreeWorkEvaluationListenerPort {

    private final DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase;
    private final ObjectMapper objectMapper;
    
    public DegreeWorkEvaluationListenerAdapter(
            DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase,
            ObjectMapper objectMapper) {
        this.degreeWorkEvaluationUseCase = degreeWorkEvaluationUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    @RabbitListener(queues = "evaluation.queue")
    public void onUpdate(DegreeWorkUpdateDTO dto) {
        System.out.println("📥 [Estado] Recibido UPDATE DTO: " + dto);
        
        // Validación temprana y específica
        if (dto == null) {
            System.err.println("⚠️ [Estado] DTO es null, ignorando mensaje");
            return;
        }
        
        if (dto.getDegreeWorkId() == null) {
            System.err.println("⚠️ [Estado] DTO inválido: degreeWorkId es null. DTO completo: " + dto);
            System.err.println("⚠️ Esto podría ser un mensaje de evaluadores mal interpretado");
            return;
        }
        
        // Verificar que sea realmente un mensaje de estado (tiene estado o correcciones)
        if (dto.getEstado() == null && dto.getCorrecciones() == null) {
            System.err.println("⚠️ [Estado] DTO sospechoso: No tiene estado ni correcciones");
            System.err.println("⚠️ Posiblemente es un mensaje de evaluadores");
            return;
        }
        
        System.out.println("✅ [Estado] Procesando cambio de estado: " + dto.getEstado());
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);
    }

    @Override
    @RabbitListener(queues = "evaluation.queue")
    public void onEvaluadores(EvaluacionEventDTO dto) {
        System.out.println("📥 [Evaluadores] Recibido EVALUADORES DTO: " + dto);
        
        // Validación temprana y específica
        if (dto == null) {
            System.err.println("⚠️ [Evaluadores] DTO es null, ignorando mensaje");
            return;
        }
        
        // Verificar campos requeridos según tu EvaluacionEventDTO
        // (ajusta según la estructura real de tu EvaluacionEventDTO)
        if (dto.getDegreeWorkId() == null) {
            System.err.println("⚠️ [Evaluadores] DTO inválido: degreeWorkId es null. DTO completo: " + dto);
            System.err.println("⚠️ Esto podría ser un mensaje de estado mal interpretado");
            return;
        }
        
        // Verificar que sea realmente un mensaje de evaluadores
        if (dto.getEvaluadores() == null || dto.getEvaluadores().isEmpty()) {
            System.err.println("⚠️ [Evaluadores] DTO sospechoso: No tiene evaluadores asignados");
            System.err.println("⚠️ Posiblemente es un mensaje de estado");
            return;
        }
        
        System.out.println("✅ [Evaluadores] Procesando " + dto.getEvaluadores().size() + " evaluadores");
        degreeWorkEvaluationUseCase.asignarEvaluadores(dto);
    }
    
    /**
     * Método adicional para debug: ver el mensaje RAW
     * Puedes comentar los otros dos métodos y usar este temporalmente
     */
    // @RabbitListener(queues = "evaluation.queue")
    public void onRawMessage(Message message) {
        try {
            String rawMessage = new String(message.getBody());
            System.out.println("🔍 MENSAJE RAW RECIBIDO:");
            System.out.println("🔍 Contenido: " + rawMessage);
            System.out.println("🔍 Headers: " + message.getMessageProperties().getHeaders());
            System.out.println("🔍 Content Type: " + message.getMessageProperties().getContentType());
            
            // Intentar deserializar como DegreeWorkUpdateDTO
            try {
                DegreeWorkUpdateDTO statusDto = objectMapper.readValue(rawMessage, DegreeWorkUpdateDTO.class);
                System.out.println("🔍 Como DegreeWorkUpdateDTO: " + statusDto);
            } catch (Exception e) {
                System.out.println("🔍 No es un DegreeWorkUpdateDTO válido");
            }
            
            // Intentar deserializar como EvaluacionEventDTO
            try {
                EvaluacionEventDTO evaluadoresDto = objectMapper.readValue(rawMessage, EvaluacionEventDTO.class);
                System.out.println("🔍 Como EvaluacionEventDTO: " + evaluadoresDto);
            } catch (Exception e) {
                System.out.println("🔍 No es un EvaluacionEventDTO válido");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error leyendo mensaje RAW: " + e.getMessage());
        }
    }
}