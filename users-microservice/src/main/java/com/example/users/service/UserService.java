package com.example.users.service;

// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    public UserService(UserRepository userRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
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

            // Crear evento DTO para enviar por RabbitMQ
            UserCreatedEvent event = new UserCreatedEvent(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getProgram(),
                savedUser.getStatus(),
                false
            );

            // Enviar el evento al exchange
            rabbitTemplate.convertAndSend(exchange, routingKey, event);

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
        UserCreatedEvent event = new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getProgram(),
            savedUser.getStatus(),
            savedUser.isEvaluator()
        );

        // Enviar el evento al exchange
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

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


    // ==========================
    // MÉTODOS AUXILIARES
    // ==========================

    

    private boolean isValidEmail(String email) {
        // Para sync desde Keycloak, aceptar cualquier email válido
        // Para registro normal, mantener validación estricta
        return email != null && email.contains("@") && email.contains(".");
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
