package com.example.users.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DepartmentHeadTest {

    @Test
    void whenCreateDepartmentHeadWithValidData_thenSuccess() {
        assertDoesNotThrow(() -> {
            DepartmentHead deptHead = new DepartmentHead("Roberto", "Silva", "1234567", "Ingeniería de Sistemas", 
                                                       "roberto@example.com", "password123");
        });
    }

    @Test
    void testDepartmentHeadDefaultStatus() {
        DepartmentHead deptHead = new DepartmentHead("Ana", "Garcia", "1234567", "Ingeniería de Sistemas", 
                                                   "ana@example.com", "password123");
        
        assertEquals("PENDIENTE", deptHead.getStatus());
        assertEquals("DEPARTMENT_HEAD", deptHead.getRole());
    }

    @Test
    void testShowDashboard() {
        DepartmentHead deptHead = new DepartmentHead();
        assertDoesNotThrow(() -> deptHead.showDashboard());
    }
}