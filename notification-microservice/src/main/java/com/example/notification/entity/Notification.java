package com.example.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientEmail;
    private String subject;
    private String message;
    private String type;
    private LocalDateTime sentAt = LocalDateTime.now();

        // Patrones de validación
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    private static final int MAX_SUBJECT_LENGTH = 255;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    // ===== Getters y Setters con validaciones =====
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un valor positivo.");
        }
        this.id = id;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }
    
    public void setRecipientEmail(String recipientEmail) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del destinatario no puede estar vacío.");
        }
        
        // PRIMERO hacer trim y lowercase, LUEGO validar
        String cleanedEmail = recipientEmail.trim().toLowerCase();
        
        if (!EMAIL_PATTERN.matcher(cleanedEmail).matches()) {
            throw new IllegalArgumentException("El correo electrónico del destinatario no es válido.");
        }
        this.recipientEmail = cleanedEmail;  // usar el email limpio
    }

    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("El asunto no puede estar vacío.");
        }
        if (subject.trim().length() > MAX_SUBJECT_LENGTH) {
            throw new IllegalArgumentException("El asunto no puede exceder los " + MAX_SUBJECT_LENGTH + " caracteres.");
        }
        this.subject = subject.trim();
    }

    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }
        if (message.trim().length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("El mensaje no puede exceder los " + MAX_MESSAGE_LENGTH + " caracteres.");
        }
        this.message = message.trim();
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de notificación no puede estar vacío.");
        }
        this.type = type;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        if (sentAt == null) {
            throw new IllegalArgumentException("La fecha de envío no puede ser nula.");
        }
        if (sentAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de envío no puede ser en el futuro.");
        }
        this.sentAt = sentAt;
    }
}

