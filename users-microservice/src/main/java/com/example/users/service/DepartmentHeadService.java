package com.example.users.service;

import com.example.users.infra.dto.EvaluatorAssignmentDTO;
import com.example.users.entity.User;
import com.example.users.repository.UserRepository;
import com.example.users.patterns.UserComponent;
import com.example.users.patterns.EvaluatorDecorator;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentHeadService {

    private final UserRepository userRepository;

    public DepartmentHeadService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Lista todos los usuarios que pueden ser evaluadores (no estudiantes)
     */
    public List<User> listPotentialEvaluators() {
        return userRepository.findAll()
                .stream()
                .filter(u -> !u.getRole().equalsIgnoreCase("STUDENT"))
                .collect(Collectors.toList());
    }

    /**
     * Asigna el rol de evaluador a un usuario específico
     */
    public String assignEvaluator(EvaluatorAssignmentDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole().equalsIgnoreCase("STUDENT")) {
            throw new IllegalArgumentException("Un estudiante no puede ser evaluador.");
        }

        // Decoramos al usuario como Evaluador
        EvaluatorDecorator evaluator = new EvaluatorDecorator(new UserComponent(user));

        // Si quieres guardar en BD una marca que indique que ya es evaluador, podrías:
        user.setStatus("EVALUADOR");
        userRepository.save(user);

        return "Usuario " + evaluator.getFullName() + " ahora es " + evaluator.getRole();
    }
}
