package com.example.evaluation.infra.messaging;

import com.example.evaluation.entity.Evaluador;
import com.example.evaluation.infra.dto.UserCreatedEvent;
import com.example.evaluation.repository.EvaluadorRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {

    private final EvaluadorRepository evaluadorRepository;

    public UserCreatedListener(EvaluadorRepository evaluadorRepository) {
        this.evaluadorRepository = evaluadorRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.user.queue}")
    public void onUserCreated(UserCreatedEvent event) {
        String nombreCompleto = (event.getFirstName() + " " + event.getLastName()).trim();
        evaluadorRepository.findByCorreo(event.getEmail())
                .ifPresentOrElse(existing -> {
                    existing.setNombre(nombreCompleto);
                    existing.setRol(event.getRole());
                    evaluadorRepository.save(existing);
                }, () -> {
                    Evaluador evaluador = new Evaluador(nombreCompleto, event.getRole(), event.getEmail());
                    evaluadorRepository.save(evaluador);
                });
    }
}
