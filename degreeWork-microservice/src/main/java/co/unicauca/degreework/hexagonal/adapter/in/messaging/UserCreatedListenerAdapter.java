package co.unicauca.degreework.hexagonal.adapter.in.messaging;

import co.unicauca.degreework.hexagonal.application.dto.UserCreatedEvent;
import co.unicauca.degreework.hexagonal.application.service.UserManagementUseCase;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.port.in.messaging.UserCreatedListenerPort;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListenerAdapter implements UserCreatedListenerPort {

    private final UserManagementUseCase userManagementUseCase;

    public UserCreatedListenerAdapter(UserManagementUseCase userManagementUseCase) {
        this.userManagementUseCase = userManagementUseCase;
    }


    @Override
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
                .isEvaluator(event.isEvaluator())
                .build();
        userManagementUseCase.save(user);
    }
}