package com.example.users.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import com.example.users.entity.enums.EnumDepartment;

// Usamos Student para testear User (ya que User es abstract)
class UserTest {

    private Student student; // Usamos Student que hereda de User

    @BeforeEach
    void setUp() {
        student = new Student();
    }

    @Test
    void whenCreateUserWithValidData_thenSuccess() {
        assertDoesNotThrow(() -> {
            Student student = new Student("Carlos", "Lopez", "1234567", "Ingeniería de Sistemas", 
                                       "carlos@example.com", "password123");
        });
    }

    @Test
    void whenSetValidProgram_thenAssignsDepartment() {
        student.setProgram("Ingeniería de Sistemas");
        assertEquals("Ingeniería de Sistemas", student.getProgram());
        assertEquals(EnumDepartment.SISTEMAS, student.getDepartment());
    }

    @Test
    void whenSetElectronicsProgram_thenAssignsElectronicaDepartment() {
        student.setProgram("Ingeniería Electrónica");
        assertEquals(EnumDepartment.ELECTRONICA, student.getDepartment());
    }

    @Test
    void whenSetCivilProgram_thenAssignsCivilDepartment() {
        student.setProgram("Ingeniería Civil");
        assertEquals(EnumDepartment.CIVIL, student.getDepartment());
    }

    @Test
    void whenSetUnknownProgram_thenDepartmentIsNull() {
        student.setProgram("Medicina");
        assertNull(student.getDepartment());
    }

    @Test
    void whenSetNullProgram_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            student.setProgram(null);
        });
        assertTrue(exception.getMessage().contains("programa"));
    }

    @Test
    void whenSetEmptyProgram_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            student.setProgram("   ");
        });
        assertTrue(exception.getMessage().contains("programa"));
    }

    @Test
    void whenSetValidRole_thenNoException() {
        assertDoesNotThrow(() -> student.setRole("STUDENT"));
        assertEquals("STUDENT", student.getRole());
    }

    @Test
    void whenSetNullRole_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            student.setRole(null);
        });
        assertTrue(exception.getMessage().contains("Rol inválido"));
    }

    @Test
    void whenSetEmptyRole_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            student.setRole("   ");
        });
        assertTrue(exception.getMessage().contains("Rol inválido"));
    }

    @Test
    void whenSetValidStatus_thenNoException() {
        assertDoesNotThrow(() -> student.setStatus("ACEPTADO"));
        assertEquals("ACEPTADO", student.getStatus());
    }

    @Test
    void whenSetNullStatus_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            student.setStatus(null);
        });
        assertTrue(exception.getMessage().contains("estado"));
    }

    @Test
    void whenSetEvaluator_thenUpdatesFlag() {
        student.setEvaluator(true);
        assertTrue(student.isEvaluator());
        
        student.setEvaluator(false);
        assertFalse(student.isEvaluator());
    }

    @Test
    void whenFirstNameWithSpaces_thenTrims() {
        student.setFirstName("  Juan  ");
        assertEquals("Juan", student.getFirstName());
    }

    @Test
    void whenLastNameWithSpaces_thenTrims() {
        student.setLastName("  Perez  ");
        assertEquals("Perez", student.getLastName());
    }

    @Test
    void whenSetValidEmail_thenNoException() {
        assertDoesNotThrow(() -> student.setEmail("test@example.com"));
        assertEquals("test@example.com", student.getEmail());
    }

    @Test
    void whenSetInvalidEmail_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            student.setEmail("invalid-email");
        });
        assertTrue(exception.getMessage().contains("correo electrónico"));
    }

    @Test
    void whenSetValidPassword_thenNoException() {
        assertDoesNotThrow(() -> student.setPassword("123456"));
        assertEquals("123456", student.getPassword());
    }

    @Test
    void whenSetShortPassword_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            student.setPassword("123");
        });
        assertTrue(exception.getMessage().contains("al menos 6 caracteres"));
    }
}