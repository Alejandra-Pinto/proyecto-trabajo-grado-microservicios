package co.unicauca.degreework.hexagonal.adapter.in.messaging;

import co.unicauca.degreework.hexagonal.application.dto.UserCreatedEvent;
import co.unicauca.degreework.hexagonal.application.service.UserManagementUseCase;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.port.in.messaging.UserCreatedListenerPort;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListenerAdapter implements UserCreatedListenerPort {

    private final UserManagementUseCase userManagementUseCase;

    @Value("${app.rabbitmq.users.queue}")
    private String userQueueName;

    public UserCreatedListenerAdapter(UserManagementUseCase userManagementUseCase) {
        this.userManagementUseCase = userManagementUseCase;
    }

    @Override
    @RabbitListener(queues = "#{@userQueue.name}") // 🔥 Referencia a la cola configurada
    public void onUserCreated(UserCreatedEvent event) {
        System.out.println("📥 Mensaje recibido en degreework - Cola: " + userQueueName);
        
        try {
            User user = User.builder()
                    .id(event.getId())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .email(event.getEmail())
                    .role(event.getRole())
                    .program(event.getProgram())
                    .status(event.getStatus())
                    .isEvaluator(event.isEvaluator())
                    .build();
            
            userManagementUseCase.save(user);
            System.out.println("✅ Usuario guardado en degreework: " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error procesando usuario en degreework: " + e.getMessage());
            // Puedes implementar reintentos o DLQ aquí
        }
    }
}