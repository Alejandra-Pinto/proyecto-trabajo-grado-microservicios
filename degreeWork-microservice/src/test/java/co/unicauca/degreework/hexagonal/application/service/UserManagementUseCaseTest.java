package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private UserManagementUseCase userManagementUseCase;

    private User testUser;

    @BeforeEach
    void setUp() {
        userManagementUseCase = new UserManagementUseCase(userRepositoryPort);
        
        // Configurar usuario de prueba según la estructura real
        testUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@unicauca.edu.co")
                .role("STUDENT")
                .program("Ingeniería de Sistemas")
                .status("ACTIVE")
                .isEvaluator(false)
                .build();
    }

    @Test
    void testFindAll() {
        // Arrange
        User secondUser = User.builder()
                .id(2L)
                .firstName("María")
                .lastName("Gómez")
                .email("maria.gomez@unicauca.edu.co")
                .role("PROFESSOR")
                .program("Ingeniería Electrónica")
                .status("ACTIVE")
                .isEvaluator(true)
                .build();
                
        List<User> expectedUsers = Arrays.asList(testUser, secondUser);
        when(userRepositoryPort.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userManagementUseCase.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(userRepositoryPort).findAll();
    }

    @Test
    void testFindAll_EmptyList() {
        // Arrange
        when(userRepositoryPort.findAll()).thenReturn(Arrays.asList());

        // Act
        List<User> result = userManagementUseCase.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepositoryPort).findAll();
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userManagementUseCase.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        assertEquals("juan.perez@unicauca.edu.co", result.get().getEmail());
        assertEquals("Juan", result.get().getFirstName());
        assertEquals("Pérez", result.get().getLastName());
        verify(userRepositoryPort).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(userRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userManagementUseCase.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepositoryPort).findById(999L);
    }

    @Test
    void testFindByEmail_Found() {
        // Arrange
        when(userRepositoryPort.findByEmail("juan.perez@unicauca.edu.co")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userManagementUseCase.findByEmail("juan.perez@unicauca.edu.co");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        assertEquals(1L, result.get().getId());
        assertEquals("STUDENT", result.get().getRole());
        verify(userRepositoryPort).findByEmail("juan.perez@unicauca.edu.co");
    }

    @Test
    void testFindByEmail_NotFound() {
        // Arrange
        when(userRepositoryPort.findByEmail("nonexistent@unicauca.edu.co")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userManagementUseCase.findByEmail("nonexistent@unicauca.edu.co");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepositoryPort).findByEmail("nonexistent@unicauca.edu.co");
    }

    @Test
    void testSave() {
        // Arrange
        User newUser = User.builder()
                .firstName("Carlos")
                .lastName("López")
                .email("carlos.lopez@unicauca.edu.co")
                .role("STUDENT")
                .program("Ingeniería Civil")
                .status("ACTIVE")
                .isEvaluator(false)
                .build();
                
        User savedUser = User.builder()
                .id(3L)
                .firstName("Carlos")
                .lastName("López")
                .email("carlos.lopez@unicauca.edu.co")
                .role("STUDENT")
                .program("Ingeniería Civil")
                .status("ACTIVE")
                .isEvaluator(false)
                .build();
        
        when(userRepositoryPort.save(newUser)).thenReturn(savedUser);

        // Act
        User result = userManagementUseCase.save(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(savedUser, result);
        assertEquals(3L, result.getId());
        assertEquals("Carlos", result.getFirstName());
        verify(userRepositoryPort).save(newUser);
    }

    @Test
    void testSave_WithExistingUser() {
        // Arrange
        User existingUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez Updated")
                .email("juan.perez@unicauca.edu.co")
                .role("STUDENT")
                .program("Ingeniería de Sistemas")
                .status("ACTIVE")
                .isEvaluator(true) // Cambiado a true
                .build();
                
        when(userRepositoryPort.save(existingUser)).thenReturn(existingUser);

        // Act
        User result = userManagementUseCase.save(existingUser);

        // Assert
        assertNotNull(result);
        assertEquals("Pérez Updated", result.getLastName());
        assertTrue(result.isEvaluator());
        verify(userRepositoryPort).save(existingUser);
    }

    @Test
    void testDeleteById() {
        // Arrange - no setup needed for void method

        // Act
        userManagementUseCase.deleteById(1L);

        // Assert
        verify(userRepositoryPort).deleteById(1L);
    }

    @Test
    void testDeleteById_WithNonExistentId() {
        // Arrange
        doNothing().when(userRepositoryPort).deleteById(999L);

        // Act
        userManagementUseCase.deleteById(999L);

        // Assert
        verify(userRepositoryPort).deleteById(999L);
    }

    @Test
    void testFindByRole() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userRepositoryPort.findByRole("STUDENT")).thenReturn(expectedUsers);

        // Act
        List<User> result = userManagementUseCase.findByRole("STUDENT");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedUsers, result);
        verify(userRepositoryPort).findByRole("STUDENT");
    }

    @Test
    void testFindByRole_EmptyResult() {
        // Arrange
        when(userRepositoryPort.findByRole("ADMIN")).thenReturn(Arrays.asList());

        // Act
        List<User> result = userManagementUseCase.findByRole("ADMIN");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepositoryPort).findByRole("ADMIN");
    }

    @Test
    void testFindByRole_NullRole() {
        // Arrange
        when(userRepositoryPort.findByRole(null)).thenReturn(Arrays.asList());

        // Act
        List<User> result = userManagementUseCase.findByRole(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepositoryPort).findByRole(null);
    }

    @Test
    void testFindByProgram() {
        // Arrange
        User secondStudent = User.builder()
                .id(2L)
                .firstName("Ana")
                .lastName("Rodríguez")
                .email("ana.rodriguez@unicauca.edu.co")
                .role("STUDENT")
                .program("Ingeniería de Sistemas")
                .status("ACTIVE")
                .isEvaluator(false)
                .build();
                
        List<User> expectedUsers = Arrays.asList(testUser, secondStudent);
        when(userRepositoryPort.findByProgram("Ingeniería de Sistemas")).thenReturn(expectedUsers);

        // Act
        List<User> result = userManagementUseCase.findByProgram("Ingeniería de Sistemas");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(userRepositoryPort).findByProgram("Ingeniería de Sistemas");
    }

    @Test
    void testFindByProgram_EmptyResult() {
        // Arrange
        when(userRepositoryPort.findByProgram("Medicina")).thenReturn(Arrays.asList());

        // Act
        List<User> result = userManagementUseCase.findByProgram("Medicina");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepositoryPort).findByProgram("Medicina");
    }

    @Test
    void testFindByProgram_NullProgram() {
        // Arrange
        when(userRepositoryPort.findByProgram(null)).thenReturn(Arrays.asList());

        // Act
        List<User> result = userManagementUseCase.findByProgram(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepositoryPort).findByProgram(null);
    }

    @Test
    void testIntegration_FindUpdateAndFind() {
        // Arrange
        User updatedUser = User.builder()
                .id(1L)
                .firstName("Juan Carlos")
                .lastName("Pérez Gómez")
                .email("juan.perez@unicauca.edu.co")
                .role("GRADUATE")
                .program("Ingeniería de Sistemas")
                .status("GRADUATED")
                .isEvaluator(true)
                .build();

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepositoryPort.save(updatedUser)).thenReturn(updatedUser);
        when(userRepositoryPort.findByEmail("juan.perez@unicauca.edu.co")).thenReturn(Optional.of(updatedUser));

        // Act - Find original
        Optional<User> originalUser = userManagementUseCase.findById(1L);

        // Assert - Original
        assertTrue(originalUser.isPresent());
        assertEquals("STUDENT", originalUser.get().getRole());
        assertFalse(originalUser.get().isEvaluator());

        // Act - Update
        User savedUser = userManagementUseCase.save(updatedUser);

        // Assert - Update
        assertNotNull(savedUser);
        assertEquals("GRADUATE", savedUser.getRole());
        assertTrue(savedUser.isEvaluator());

        // Act - Find by email after update
        Optional<User> foundAfterUpdate = userManagementUseCase.findByEmail("juan.perez@unicauca.edu.co");

        // Assert - Find after update
        assertTrue(foundAfterUpdate.isPresent());
        assertEquals("GRADUATED", foundAfterUpdate.get().getStatus());

        verify(userRepositoryPort).findById(1L);
        verify(userRepositoryPort).save(updatedUser);
        verify(userRepositoryPort).findByEmail("juan.perez@unicauca.edu.co");
    }

    @Test
    void testUserBuilderPattern() {
        // Test para verificar que el patrón Builder funciona correctamente
        User user = User.builder()
                .id(5L)
                .firstName("Test")
                .lastName("User")
                .email("test@unicauca.edu.co")
                .role("PROFESSOR")
                .program("Ingeniería Industrial")
                .status("ACTIVE")
                .isEvaluator(true)
                .build();

        assertNotNull(user);
        assertEquals(5L, user.getId());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("test@unicauca.edu.co", user.getEmail());
        assertEquals("PROFESSOR", user.getRole());
        assertEquals("Ingeniería Industrial", user.getProgram());
        assertEquals("ACTIVE", user.getStatus());
        assertTrue(user.isEvaluator());
    }
}