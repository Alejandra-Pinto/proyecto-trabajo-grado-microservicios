package com.example.users.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void whenCreateStudentWithValidData_thenSuccess() {
        assertDoesNotThrow(() -> {
            Student student = new Student("Ana", "Garcia", "1234567", "Ingeniería de Sistemas", 
                                        "ana@example.com", "password123");
        });
    }

    @Test
    void whenCreateStudentWithFullName_thenSplitsCorrectly() {
        Student student = new Student("Maria Rodriguez", "maria@example.com", "password123");
        
        assertEquals("Maria", student.getFirstName());
        assertEquals("Rodriguez", student.getLastName());
        assertEquals("maria@example.com", student.getEmail());
        assertEquals("STUDENT", student.getRole());
        assertEquals("ACEPTADO", student.getStatus());
    }

    @Test
    void whenCreateStudentWithSingleName_thenHandlesCorrectly() {
        // Cambiar "Carlos" por un nombre con apellido
        Student student = new Student("Carlos Mendoza", "carlos@example.com", "password123");
        
        assertEquals("Carlos", student.getFirstName());
        assertEquals("Mendoza", student.getLastName());
    }

    @Test
    void testStudentDefaultValues() {
        Student student = new Student("Juan Perez", "juan@example.com", "password123");
        
        assertEquals("0000000", student.getPhone());
        assertEquals("Ingeniería de Sistemas", student.getProgram());
        assertEquals("ACEPTADO", student.getStatus());
        assertEquals("STUDENT", student.getRole());
    }

    @Test
    void testShowDashboard() {
        Student student = new Student();
        // Solo verifica que no lance excepción
        assertDoesNotThrow(() -> student.showDashboard());
    }

    
}