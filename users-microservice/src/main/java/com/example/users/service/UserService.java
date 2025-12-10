package com.example.users.service;

// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.users.entity.Coordinator;
import com.example.users.entity.DepartmentHead;
import com.example.users.entity.Student;
import com.example.users.entity.Teacher;
import com.example.users.entity.User;
import com.example.users.repository.UserRepository;
import com.example.users.infra.dto.*;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.rabbitmq.users.exchange}")
    private String exchange;

    // 🔥 CAMBIO: Hacer el routingKey opcional con valor por defecto
    @Value("${app.rabbitmq.users.routingkey:}")
    private String routingKey;

    public UserService(UserRepository userRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // 🔥 CAMBIO: Método auxiliar para enviar eventos (mantiene la lógica existente)
    private void sendUserEvent(User savedUser, boolean isEvaluator) {
        UserCreatedEvent event = new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getProgram(),
            savedUser.getStatus(),
            isEvaluator
        );

        // 🔥 CAMBIO: Lógica adaptada para Fanout Exchange
        if (routingKey != null && !routingKey.trim().isEmpty()) {
            // Si hay routing key (compatibilidad con Direct Exchange)
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } else {
            // Para Fanout Exchange - routing key vacío
            rabbitTemplate.convertAndSend(exchange, "", event);
        }

        System.out.println("✅ Evento enviado al exchange: " + exchange);
    }

    private User createUserByRole(String role) {
        return switch (role.toUpperCase()) {
            case "STUDENT" -> new Student();
            case "PROFESSOR" -> new Teacher();
            case "COORDINATOR" -> new Coordinator();
            case "DEPARTMENT_HEAD" -> new DepartmentHead();
            default -> throw new IllegalArgumentException("Rol no reconocido: " + role);
        };
    }


    @Override
    public User syncUserFromKeycloak(UserRequest request) {
        try {
            System.out.println("=== SYNC USER SERVICE ===");
            
            // Crear instancia según el rol
            User user = createUserByRole(request.getRole());

            // Asignar atributos
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(request.getPhone() != null ? request.getPhone() : "");
            user.setProgram(request.getProgram());
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            user.setStatus(request.getStatus());

            // ⚠️ IMPORTANTE: No validar email ni password para sync desde Keycloak
            // Keycloak ya validó el email y maneja la autenticación
            
            // ⚠️ NO cifrar la password - usar un valor dummy ya que la auth es con Keycloak
            user.setPassword("keycloak-managed-password");

            // Guardar en la base de datos
            User savedUser = userRepository.save(user);

            sendUserEvent(savedUser, false);

            System.out.println("✅ User sync completed: " + savedUser.getEmail());
            return savedUser;

        } catch (Exception e) {
            System.out.println("❌ Error in user sync: " + e.getMessage());
            throw new RuntimeException("Error sincronizando usuario desde Keycloak: " + e.getMessage());
        }
    }
    @Override
    public User register(UserRequest request) {
        // Crear instancia según el rol
        User user = createUserByRole(request.getRole());

        // Asignar atributos
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setProgram(request.getProgram());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());

        // Validaciones
        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email inválido. Debe ser institucional @unicauca.edu.co");
        }

        if (!isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Contraseña insegura. Debe tener al menos 6 caracteres, un número, un caracter especial y una mayúscula.");
        }

        // Cifrar la contraseña
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Guardar en la base de datos
        User savedUser = userRepository.save(user);

        // Crear evento DTO para enviar por RabbitMQ
        sendUserEvent(savedUser, savedUser.isEvaluator());

        return savedUser;
    }



    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!"ACEPTADO".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException(
                "Tu cuenta está en estado " + user.getStatus() + ". No puedes iniciar sesión."
            );
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta.");
        }

        return user;
    }

    public String logout(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        return "Sesión cerrada correctamente para " + email;
    }


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Override
    public List<User> findByRole(String role) {
        return userRepository.findByRoleIgnoreCase(role);
    }

    @Override
    public List<User> listAssignedEvaluators() {
        return userRepository.findAll()
                .stream()
                .filter(User::isEvaluator) // solo quienes ya son evaluadores
                .collect(Collectors.toList());
    }

    // ==========================
    // MÉTODOS AUXILIARES
    // ==========================

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        // Validar formato básico
        if (!email.contains("@") || !email.contains(".")) return false;
        
        // Para registro normal, debe ser email institucional
        return email.toLowerCase().endsWith("@unicauca.edu.co");
    }

    // O crear un método separado para sync
    private boolean isValidEmailForSync(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) return false;
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*]).+$";
        return Pattern.matches(regex, password);
    }

}
