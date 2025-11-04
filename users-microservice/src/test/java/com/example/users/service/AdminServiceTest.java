package com.example.users.service;

import com.example.users.entity.Admin;
import com.example.users.entity.User;
import com.example.users.repository.AdminRepository;
import com.example.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private UserRepository userRepository;

    // Mock manual del passwordEncoder
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AdminService adminService;

    private Admin admin;
    private User coordinator;

    @BeforeEach
    void setUp() {
        // Inicializar el servicio con los mocks manuales
        adminService = new AdminService(adminRepository, userRepository);
        
        admin = new Admin("Admin", "User", "1234567", "admin@unicauca.edu.co", "password123");
        
        coordinator = new User("Coordinator", "User", "1234567", "Ingeniería de Sistemas", 
                              "coordinator@unicauca.edu.co", "password123", "COORDINATOR") {
            @Override
            public void showDashboard() {}
        };
        coordinator.setStatus("PENDIENTE");
    }

    @Test
    void whenRegisterAdmin_thenEncryptPasswordAndSave() {
        // Given
        Admin newAdmin = new Admin("New", "Admin", "1234567", "newadmin@unicauca.edu.co", "plainPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(newAdmin);

        // When
        Admin result = adminService.register(newAdmin);

        // Then
        // Verificar que la contraseña fue encriptada (no debería ser la misma)
        assertNotEquals("plainPassword", result.getPassword());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void whenApproveExistingCoordinator_thenSuccess() {
        // Given
        when(userRepository.findByEmail("coordinator@unicauca.edu.co"))
                .thenReturn(Optional.of(coordinator));
        when(userRepository.save(any(User.class))).thenReturn(coordinator);

        // When
        String result = adminService.approveUser("coordinator@unicauca.edu.co");

        // Then
        assertEquals("ACEPTADO", coordinator.getStatus());
        assertTrue(result.contains("aprobado"));
        verify(userRepository).save(coordinator);
    }

    @Test
    void whenApproveNonExistingUser_thenReturnsErrorMessage() {
        // Given
        when(userRepository.findByEmail("nonexistent@unicauca.edu.co"))
                .thenReturn(Optional.empty());

        // When
        String result = adminService.approveUser("nonexistent@unicauca.edu.co");

        // Then
        assertTrue(result.contains("no encontrado"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenApproveStudent_thenReturnsErrorMessage() {
        // Given
        User student = new User("Student", "User", "1234567", "Ingeniería de Sistemas", 
                               "student@unicauca.edu.co", "password123", "STUDENT") {
            @Override
            public void showDashboard() {}
        };
        student.setStatus("ACEPTADO");

        when(userRepository.findByEmail("student@unicauca.edu.co"))
                .thenReturn(Optional.of(student));

        // When
        String result = adminService.approveUser("student@unicauca.edu.co");

        // Then
        assertTrue(result.contains("Solo se pueden aprobar roles"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenRejectCoordinator_thenUpdatesStatus() {
        // Given
        when(userRepository.findByEmail("coordinator@unicauca.edu.co"))
                .thenReturn(Optional.of(coordinator));
        when(userRepository.save(any(User.class))).thenReturn(coordinator);

        // When
        String result = adminService.rejectUser("coordinator@unicauca.edu.co");

        // Then
        assertEquals("RECHAZADO", coordinator.getStatus());
        assertTrue(result.contains("rechazado"));
        verify(userRepository).save(coordinator);
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnsAdmin() {
        // Given
        String encodedPassword = passwordEncoder.encode("password123");
        Admin adminWithEncodedPassword = new Admin("Admin", "User", "1234567", 
                                                  "admin@unicauca.edu.co", encodedPassword);
        
        when(adminRepository.findByEmail("admin@unicauca.edu.co"))
                .thenReturn(Optional.of(adminWithEncodedPassword));

        // When
        Optional<Admin> result = adminService.login("admin@unicauca.edu.co", "password123");

        // Then
        assertTrue(result.isPresent());
        assertEquals(adminWithEncodedPassword, result.get());
    }

    @Test
    void whenLoginWithInvalidPassword_thenReturnsEmpty() {
        // Given
        String encodedPassword = passwordEncoder.encode("correctpassword");
        Admin adminWithEncodedPassword = new Admin("Admin", "User", "1234567", 
                                                  "admin@unicauca.edu.co", encodedPassword);
        
        when(adminRepository.findByEmail("admin@unicauca.edu.co"))
                .thenReturn(Optional.of(adminWithEncodedPassword));

        // When
        Optional<Admin> result = adminService.login("admin@unicauca.edu.co", "wrongpassword");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void whenListPotentialEvaluators_thenReturnsNonStudents() {
        // Given
        User teacher = new User("Teacher", "User", "1234567", "Ingeniería de Sistemas", 
                               "teacher@unicauca.edu.co", "password123", "TEACHER") {
            @Override
            public void showDashboard() {}
        };
        teacher.setStatus("ACEPTADO");

        User student = new User("Student", "User", "1234567", "Ingeniería de Sistemas", 
                               "student@unicauca.edu.co", "password123", "STUDENT") {
            @Override
            public void showDashboard() {}
        };
        student.setStatus("ACEPTADO");

        List<User> allUsers = List.of(teacher, coordinator, student);
        when(userRepository.findAll()).thenReturn(allUsers);

        // When
        List<User> result = adminService.listPotentialEvaluators();

        // Then
        assertEquals(2, result.size()); // teacher y coordinator, no student
        assertTrue(result.stream().noneMatch(u -> "STUDENT".equalsIgnoreCase(u.getRole())));
    }
}