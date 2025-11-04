package com.example.notification.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class NotificationTest {

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
    }

    @Test
    void whenSetValidEmail_thenNoException() {
        assertDoesNotThrow(() -> {
            notification.setRecipientEmail("usuario@dominio.com");
        });
        assertEquals("usuario@dominio.com", notification.getRecipientEmail());
    }

    @Test
    void whenSetEmailWithSpaces_thenTrimsAndLowercases() {
        notification.setRecipientEmail("  Usuario@Dominio.COM  ");
        assertEquals("usuario@dominio.com", notification.getRecipientEmail());
    }

    @Test
    void whenSetNullEmail_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setRecipientEmail(null);
        });
        assertTrue(exception.getMessage().contains("no puede estar vacío"));
    }

    @Test
    void whenSetEmptyEmail_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setRecipientEmail("");
        });
        assertTrue(exception.getMessage().contains("no puede estar vacío"));
    }

    @Test
    void whenSetInvalidEmail_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setRecipientEmail("email-invalido");
        });
        assertTrue(exception.getMessage().contains("no es válido"));
    }

    @Test
    void whenSetValidSubject_thenNoException() {
        assertDoesNotThrow(() -> {
            notification.setSubject("Asunto de prueba");
        });
        assertEquals("Asunto de prueba", notification.getSubject());
    }

    @Test
    void whenSetSubjectWithSpaces_thenTrims() {
        notification.setSubject("  Asunto con espacios  ");
        assertEquals("Asunto con espacios", notification.getSubject());
    }

    @Test
    void whenSetNullSubject_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setSubject(null);
        });
        assertTrue(exception.getMessage().contains("asunto"));
    }

    @Test
    void whenSetEmptySubject_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setSubject("   ");
        });
        assertTrue(exception.getMessage().contains("asunto"));
    }

    @Test
    void whenSetSubjectTooLong_thenThrowsException() {
        String subjectLargo = "A".repeat(256);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setSubject(subjectLargo);
        });
        assertTrue(exception.getMessage().contains("255"));
    }

    @Test
    void whenSetValidMessage_thenNoException() {
        assertDoesNotThrow(() -> {
            notification.setMessage("Mensaje de prueba");
        });
        assertEquals("Mensaje de prueba", notification.getMessage());
    }

    @Test
    void whenSetNullMessage_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setMessage(null);
        });
        assertTrue(exception.getMessage().contains("mensaje"));
    }

    @Test
    void whenSetMessageTooLong_thenThrowsException() {
        String mensajeLargo = "A".repeat(2001);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setMessage(mensajeLargo);
        });
        assertTrue(exception.getMessage().contains("2000"));
    }

    @Test
    void whenSetValidType_thenNoException() {
        assertDoesNotThrow(() -> {
            notification.setType("EMAIL");
        });
        assertEquals("EMAIL", notification.getType());
    }

    @Test
    void whenSetNullType_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setType(null);
        });
        assertTrue(exception.getMessage().contains("tipo de notificación"));
    }

    @Test
    void whenSetValidSentAt_thenNoException() {
        LocalDateTime fechaPasada = LocalDateTime.now().minusHours(1);
        
        assertDoesNotThrow(() -> {
            notification.setSentAt(fechaPasada);
        });
        assertEquals(fechaPasada, notification.getSentAt());
    }

    @Test
    void whenSetNullSentAt_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setSentAt(null);
        });
        assertTrue(exception.getMessage().contains("fecha de envío"));
    }

    @Test
    void whenSetFutureSentAt_thenThrowsException() {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notification.setSentAt(fechaFutura);
        });
        assertTrue(exception.getMessage().contains("futuro"));
    }
}