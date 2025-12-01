package co.unicauca.degreework.infra.messaging;

import co.unicauca.degreework.domain.entities.User;
import co.unicauca.degreework.infra.dto.UserCreatedEvent;
import co.unicauca.degreework.access.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {

    private final UserRepository userRepository;

    @Value("${app.rabbitmq.users.queue}")
    private String usersQueueName;

    public UserCreatedListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = "#{@usersQueue.name}") // 🔥 Cola específica para evaluaciones
    public void onUserCreated(UserCreatedEvent event) {
        System.out.println("📥 Evaluaciones - Usuario recibido: " + event.getEmail() + " desde cola: " + usersQueueName);
        
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
            
            userRepository.save(user);
            System.out.println("✅ Usuario guardado en evaluaciones: " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error guardando usuario en evaluaciones: " + e.getMessage());
            // Puedes implementar lógica de reintento o DLQ aquí
        }
    }
}