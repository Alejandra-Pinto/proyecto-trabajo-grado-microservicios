package com.example.notification.service;

import com.example.notification.entity.User;
import com.example.notification.infra.config.dto.UserCreatedEvent;
import com.example.notification.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserSyncService {

    private final UserRepository userRepository;

    public UserSyncService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.users.queue}")
    public void consumeUserCreated(UserCreatedEvent event) {

        System.out.println("📩 [NOTIFICATION] Usuario recibido por RabbitMQ: " + event.getEmail());

        User user = new User();
        user.setId(event.getId());
        user.setFirstName(event.getFirstName());
        user.setLastName(event.getLastName());
        user.setEmail(event.getEmail());
        user.setRole(event.getRole());
        user.setProgram(event.getProgram());
        user.setStatus(event.getStatus());
        user.setEvaluator(event.isEvaluator());

        userRepository.save(user);

        System.out.println("💾 [NOTIFICATION] Usuario guardado para futuras notificaciones");
    }
}
