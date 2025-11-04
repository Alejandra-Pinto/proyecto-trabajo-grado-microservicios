package com.example.evaluation.infra.messaging;

import com.example.evaluation.entity.User;
import com.example.evaluation.infra.dto.UserCreatedEvent;
import com.example.evaluation.repository.UserRepository;
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
