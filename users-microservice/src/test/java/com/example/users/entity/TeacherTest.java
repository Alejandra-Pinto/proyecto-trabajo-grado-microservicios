package com.example.users.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeacherTest {

    @Test
    void whenCreateTeacherWithValidData_thenSuccess() {
        assertDoesNotThrow(() -> {
            Teacher teacher = new Teacher("Pedro", "Martinez", "1234567", "Ingeniería de Sistemas", 
                                        "pedro@example.com", "password123");
        });
    }

    @Test
    void whenCreateTeacherWithFullName_thenSplitsCorrectly() {
        Teacher teacher = new Teacher("Laura Gonzalez", "laura@example.com");
        
        assertEquals("Laura", teacher.getFirstName());
        assertEquals("Gonzalez", teacher.getLastName());
        assertEquals("laura@example.com", teacher.getEmail());
        assertEquals("TEACHER", teacher.getRole());
        assertEquals("ACEPTADO", teacher.getStatus());
    }

    @Test
    void testTeacherDefaultValues() {
        Teacher teacher = new Teacher("Carlos Ruiz", "carlos@example.com");
        
        assertEquals("Desconocido", teacher.getProgram());
        assertEquals("default123", teacher.getPassword());
        assertEquals("ACEPTADO", teacher.getStatus());
        assertEquals("TEACHER", teacher.getRole());
    }

    @Test
    void testShowDashboard() {
        Teacher teacher = new Teacher();
        assertDoesNotThrow(() -> teacher.showDashboard());
    }
}