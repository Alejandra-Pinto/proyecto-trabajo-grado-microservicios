package co.unicauca.degreework.service;

import co.unicauca.degreework.access.UserRepository;
import co.unicauca.degreework.domain.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User studentUser;
    private User teacherUser;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(1L);
        studentUser.setFirstName("John");
        studentUser.setLastName("Doe");
        studentUser.setEmail("john.doe@unicauca.edu.co");
        studentUser.setRole("STUDENT");
        studentUser.setProgram("Ingeniería de Sistemas");

        teacherUser = new User();
        teacherUser.setId(2L);
        teacherUser.setFirstName("Jane");
        teacherUser.setLastName("Smith");
        teacherUser.setEmail("jane.smith@unicauca.edu.co");
        teacherUser.setRole("TEACHER");
        teacherUser.setProgram("Ingeniería de Sistemas");
    }

    @Test
    void whenListarUsuarios_thenReturnsAllUsers() {
        // Given
        List<User> users = List.of(studentUser, teacherUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.listarUsuarios();

        // Then
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void whenObtenerPorIdExisting_thenReturnsUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(studentUser));

        // When
        User result = userService.obtenerPorId(1L);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("STUDENT", result.getRole());
        verify(userRepository).findById(1L);
    }

    @Test
    void whenObtenerPorIdNonExisting_thenReturnsNull() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        User result = userService.obtenerPorId(999L);

        // Then
        assertNull(result);
        verify(userRepository).findById(999L);
    }

    @Test
    void whenObtenerPorEmailExisting_thenReturnsUser() {
        // Given
        when(userRepository.findByEmail("john.doe@unicauca.edu.co"))
                .thenReturn(Optional.of(studentUser));

        // When
        User result = userService.obtenerPorEmail("john.doe@unicauca.edu.co");

        // Then
        assertNotNull(result);
        assertEquals("john.doe@unicauca.edu.co", result.getEmail());
        verify(userRepository).findByEmail("john.doe@unicauca.edu.co");
    }

    @Test
    void whenObtenerPorEmailNonExisting_thenReturnsNull() {
        // Given
        when(userRepository.findByEmail("nonexistent@unicauca.edu.co"))
                .thenReturn(Optional.empty());

        // When
        User result = userService.obtenerPorEmail("nonexistent@unicauca.edu.co");

        // Then
        assertNull(result);
        verify(userRepository).findByEmail("nonexistent@unicauca.edu.co");
    }

    @Test
    void whenGuardarUsuario_thenSavesAndReturnsUser() {
        // Given
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("new.user@unicauca.edu.co");
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.guardarUsuario(newUser);

        // Then
        assertNotNull(result);
        verify(userRepository).save(newUser);
    }

    @Test
    void whenEliminarUsuario_thenCallsDelete() {
        // When
        userService.eliminarUsuario(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }

    @Test
    void whenListarPorRol_thenReturnsFilteredUsers() {
        // Given
        List<User> students = List.of(studentUser);
        when(userRepository.findByRole("STUDENT")).thenReturn(students);

        // When
        List<User> result = userService.listarPorRol("STUDENT");

        // Then
        assertEquals(1, result.size());
        assertEquals("STUDENT", result.get(0).getRole());
        verify(userRepository).findByRole("STUDENT");
    }

    @Test
    void whenListarPorPrograma_thenReturnsFilteredUsers() {
        // Given
        List<User> systemUsers = List.of(studentUser, teacherUser);
        when(userRepository.findByProgram("Ingeniería de Sistemas")).thenReturn(systemUsers);

        // When
        List<User> result = userService.listarPorPrograma("Ingeniería de Sistemas");

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> "Ingeniería de Sistemas".equals(u.getProgram())));
        verify(userRepository).findByProgram("Ingeniería de Sistemas");
    }
}