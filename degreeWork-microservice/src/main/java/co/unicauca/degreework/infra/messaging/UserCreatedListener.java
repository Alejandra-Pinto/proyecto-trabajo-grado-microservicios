package co.unicauca.degreework.infra.messaging;

import co.unicauca.degreework.domain.entities.User;
import co.unicauca.degreework.infra.dto.UserCreatedEvent;
import co.unicauca.degreework.access.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {

    private final UserRepository userRepository;

    public UserCreatedListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = "users.queue")
    public void onUserCreated(UserCreatedEvent event) {
        User user = User.builder()
                .id(event.getId())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .email(event.getEmail())
                .role(event.getRole())
                .program(event.getProgram())
                .status(event.getStatus())
                .build();
        userRepository.save(user);
    }
}
