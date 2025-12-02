package com.example.notification.service;

import com.example.notification.entity.User;
import com.example.notification.infra.config.dto.UserCreatedEvent;
import com.example.notification.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSyncService {

    @Autowired
    private UserRepository userRepository;

    @RabbitListener(queues = "${app.rabbitmq.users.queue}")
    public void syncUser(UserCreatedEvent event) {
        System.out.println("=== SINCRONIZANDO USUARIO ===");
        System.out.println("Email: " + event.getEmail());
        System.out.println("Rol: " + event.getRole());
        System.out.println("Nombre: " + event.getFirstName() + " " + event.getLastName());
        
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
        System.out.println("Usuario guardado exitosamente");
    }
}