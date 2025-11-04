package com.example.users.service;

import com.example.users.entity.*;
import com.example.users.repository.UserRepository;
import com.example.users.infra.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    // Necesitamos mockear el password encoder para controlar las respuestas
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private UserRequest validUserRequest;
    private User studentUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, rabbitTemplate);
        
        validUserRequest = new UserRequest();
        validUserRequest.setFirstName("John");
        validUserRequest.setLastName("Doe");
        validUserRequest.setEmail("john.doe@unicauca.edu.co");
        validUserRequest.setPassword("Secure123!");
        validUserRequest.setRole("STUDENT");
        validUserRequest.setPhone("1234567");
        validUserRequest.setProgram("Ingeniería de Sistemas");
        validUserRequest.setStatus("ACEPTADO");

        // Crear usuario con contraseña encriptada real para los tests de login
        String encodedPassword = passwordEncoder.encode("Secure123!");
        studentUser = new Student("John", "Doe", "1234567", "Ingeniería de Sistemas", 
                                 "john.doe@unicauca.edu.co", encodedPassword);
        studentUser.setStatus("ACEPTADO");
    }



    @Test
    void whenRegisterWithInvalidEmail_thenThrowsException() {
        // Given
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setEmail("invalid@gmail.com");
        invalidRequest.setPassword("Secure123!");
        invalidRequest.setRole("STUDENT");
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName("User");
        invalidRequest.setPhone("1234567");
        invalidRequest.setProgram("Ingeniería de Sistemas"); // Agregar programa
        invalidRequest.setStatus("ACEPTADO");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(invalidRequest);
        });
        assertTrue(exception.getMessage().contains("Email inválido"));
    }

    @Test
    void whenRegisterWithWeakPassword_thenThrowsException() {
        // Given
        UserRequest weakPasswordRequest = new UserRequest();
        weakPasswordRequest.setEmail("test@unicauca.edu.co");
        weakPasswordRequest.setPassword("weak"); // Contraseña débil
        weakPasswordRequest.setRole("STUDENT");
        weakPasswordRequest.setFirstName("Test");
        weakPasswordRequest.setLastName("User");
        weakPasswordRequest.setPhone("1234567");
        weakPasswordRequest.setProgram("Ingeniería de Sistemas"); // Agregar programa
        weakPasswordRequest.setStatus("ACEPTADO");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(weakPasswordRequest);
        });
        assertTrue(exception.getMessage().contains("Contraseña insegura"));
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnsUser() {
        // Given
        // Usar el usuario con contraseña encriptada correctamente
        when(userRepository.findByEmail("john.doe@unicauca.edu.co"))
                .thenReturn(Optional.of(studentUser));

        // When
        User result = userService.login("john.doe@unicauca.edu.co", "Secure123!");

        // Then
        assertNotNull(result);
        assertEquals(studentUser, result);
    }

    @Test
    void whenLoginWithNonExistingEmail_thenThrowsException() {
        // Given
        when(userRepository.findByEmail("nonexistent@unicauca.edu.co"))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login("nonexistent@unicauca.edu.co", "password");
        });
        assertEquals("Usuario no encontrado.", exception.getMessage());
    }

    @Test
    void whenLoginWithPendingStatus_thenThrowsException() {
        // Given
        User pendingUser = new Student("Pending", "User", "1234567", "Ingeniería de Sistemas", 
                                      "pending@unicauca.edu.co", "encodedPassword");
        pendingUser.setStatus("PENDIENTE");

        when(userRepository.findByEmail("pending@unicauca.edu.co"))
                .thenReturn(Optional.of(pendingUser));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.login("pending@unicauca.edu.co", "password");
        });
        assertTrue(exception.getMessage().contains("estado"));
    }

    @Test
    void whenLoginWithWrongPassword_thenThrowsException() {
        // Given
        // Crear usuario con contraseña encriptada para "correctpassword"
        String encodedPassword = passwordEncoder.encode("correctpassword");
        User userWithPassword = new Student("John", "Doe", "1234567", "Ingeniería de Sistemas", 
                                           "john.doe@unicauca.edu.co", encodedPassword);
        userWithPassword.setStatus("ACEPTADO");

        when(userRepository.findByEmail("john.doe@unicauca.edu.co"))
                .thenReturn(Optional.of(userWithPassword));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login("john.doe@unicauca.edu.co", "wrongpassword");
        });
        assertEquals("Contraseña incorrecta.", exception.getMessage());
    }

    @Test
    void whenLogoutExistingUser_thenSuccess() {
        // Given
        when(userRepository.findByEmail("john.doe@unicauca.edu.co"))
                .thenReturn(Optional.of(studentUser));

        // When
        String result = userService.logout("john.doe@unicauca.edu.co");

        // Then
        assertEquals("Sesión cerrada correctamente para john.doe@unicauca.edu.co", result);
    }

    @Test
    void whenLogoutNonExistingUser_thenThrowsException() {
        // Given
        when(userRepository.findByEmail("nonexistent@unicauca.edu.co"))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.logout("nonexistent@unicauca.edu.co");
        });
        assertEquals("Usuario no encontrado.", exception.getMessage());
    }

    @Test
    void whenGetAllUsers_thenReturnsList() {
        // Given
        List<User> users = List.of(studentUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(1, result.size());
        assertEquals(studentUser, result.get(0));
    }

    @Test
    void whenFindByEmailExisting_thenReturnsUser() {
        // Given
        when(userRepository.findByEmail("john.doe@unicauca.edu.co"))
                .thenReturn(Optional.of(studentUser));

        // When
        Optional<User> result = userService.findByEmail("john.doe@unicauca.edu.co");

        // Then
        assertTrue(result.isPresent());
        assertEquals(studentUser, result.get());
    }

    @Test
    void testCreateUserByRole() {
        // This tests the private method indirectly through register
        UserRequest teacherRequest = new UserRequest();
        teacherRequest.setRole("PROFESSOR");
        teacherRequest.setEmail("teacher@unicauca.edu.co");
        teacherRequest.setPassword("Secure123!");
        teacherRequest.setFirstName("Teacher");
        teacherRequest.setLastName("User");
        teacherRequest.setPhone("1234567");
        teacherRequest.setProgram("Ingeniería de Sistemas");
        teacherRequest.setStatus("ACEPTADO");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.register(teacherRequest);

        // Then
        assertTrue(result instanceof Teacher);
        assertEquals("PROFESSOR", result.getRole());
    }

    @Test
    void whenCreateUserWithInvalidRole_thenThrowsException() {
        // Given
        UserRequest invalidRoleRequest = new UserRequest();
        invalidRoleRequest.setRole("INVALID_ROLE");
        invalidRoleRequest.setEmail("test@unicauca.edu.co");
        invalidRoleRequest.setPassword("Secure123!");
        invalidRoleRequest.setFirstName("Test");
        invalidRoleRequest.setLastName("User");
        invalidRoleRequest.setPhone("1234567");
        invalidRoleRequest.setProgram("Ingeniería de Sistemas");
        invalidRoleRequest.setStatus("ACEPTADO");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(invalidRoleRequest);
        });
        assertTrue(exception.getMessage().contains("Rol no reconocido"));
    }

    // Test adicional para verificar que se encripta la contraseña
    @Test
    void whenRegisterUser_thenPasswordIsEncrypted() {
        // Given
        UserRequest request = new UserRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@unicauca.edu.co");
        request.setPassword("PlainPassword123!");
        request.setRole("STUDENT");
        request.setPhone("1234567");
        request.setProgram("Ingeniería de Sistemas");
        request.setStatus("ACEPTADO");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verificar que la contraseña no es la original
            assertNotEquals("PlainPassword123!", savedUser.getPassword());
            // Verificar que es un hash BCrypt (empieza con $2a$)
            assertTrue(savedUser.getPassword().startsWith("$2a$"));
            return savedUser;
        });

        // When
        userService.register(request);

        // Then - Las verificaciones se hacen en el thenAnswer
    }
}