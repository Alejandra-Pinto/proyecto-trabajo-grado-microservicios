package com.example.users.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin();
    }

    @Test
    void whenCreateAdminWithValidData_thenSuccess() {
        assertDoesNotThrow(() -> {
            Admin admin = new Admin("Juan", "Perez", "1234567", "juan@example.com", "password123");
        });
    }

    @Test
    void whenSetValidFirstName_thenNoException() {
        assertDoesNotThrow(() -> admin.setFirstName("Maria"));
        assertEquals("Maria", admin.getFirstName());
    }

    @Test
    void whenSetNullFirstName_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            admin.setFirstName(null);
        });
        assertTrue(exception.getMessage().contains("nombre"));
    }

    @Test
    void whenSetEmptyFirstName_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            admin.setFirstName("   ");
        });
        assertTrue(exception.getMessage().contains("nombre"));
    }

    @Test
    void whenSetValidLastName_thenNoException() {
        assertDoesNotThrow(() -> admin.setLastName("Gomez"));
        assertEquals("Gomez", admin.getLastName());
    }

    @Test
    void whenSetNullLastName_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            admin.setLastName(null);
        });
        assertTrue(exception.getMessage().contains("apellido"));
    }

    @Test
    void whenSetValidPhone_thenNoException() {
        assertDoesNotThrow(() -> admin.setPhone("1234567890"));
        assertEquals("1234567890", admin.getPhone());
    }

    @Test
    void whenSetInvalidPhone_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            admin.setPhone("123"); // muy corto
        });
        assertTrue(exception.getMessage().contains("teléfono"));
    }

    @Test
    void whenSetNullPhone_thenNoException() {
        assertDoesNotThrow(() -> admin.setPhone(null));
        assertNull(admin.getPhone());
    }

    @Test
    void whenSetValidEmail_thenNoException() {
        assertDoesNotThrow(() -> admin.setEmail("test@example.com"));
        assertEquals("test@example.com", admin.getEmail());
    }

    @Test
    void whenSetInvalidEmail_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            admin.setEmail("invalid-email");
        });
        assertTrue(exception.getMessage().contains("correo electrónico"));
    }

    @Test
    void whenSetEmailWithSpaces_thenTrimsAndLowercases() {
        admin.setEmail("  Test@Example.COM  ");
        assertEquals("test@example.com", admin.getEmail());
    }

    @Test
    void whenSetValidPassword_thenNoException() {
        assertDoesNotThrow(() -> admin.setPassword("123456"));
        assertEquals("123456", admin.getPassword());
    }

    @Test
    void whenSetShortPassword_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            admin.setPassword("123");
        });
        assertTrue(exception.getMessage().contains("al menos 6 caracteres"));
    }

    @Test
    void whenSetNullPassword_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            admin.setPassword(null);
        });
        assertTrue(exception.getMessage().contains("al menos 6 caracteres"));
    }

    @Test
    void testDefaultRoleIsAdmin() {
        assertEquals("ADMIN", admin.getRole());
    }
}