package com.example.users.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoordinatorTest {

    @Test
    void whenCreateCoordinatorWithValidData_thenSuccess() {
        assertDoesNotThrow(() -> {
            Coordinator coordinator = new Coordinator("Marta", "Lopez", "1234567", "Ingeniería de Sistemas", 
                                                    "marta@example.com", "password123");
        });
    }

    @Test
    void testCoordinatorDefaultStatus() {
        Coordinator coordinator = new Coordinator("Juan", "Perez", "1234567", "Ingeniería de Sistemas", 
                                                "juan@example.com", "password123");
        
        assertEquals("PENDIENTE", coordinator.getStatus());
        assertEquals("COORDINATOR", coordinator.getRole());
    }

    @Test
    void testShowDashboard() {
        Coordinator coordinator = new Coordinator();
        assertDoesNotThrow(() -> coordinator.showDashboard());
    }
}