package com.unicauca.front.model;

import java.time.LocalDateTime;

public class Notification {
    private Long id;
    private String recipientEmail;
    private String subject;
    private String message;
    private String type;
    private LocalDateTime sentAt;
    private boolean read; // Campo adicional para el frontend

    // Constructores
    public Notification() {}

    public Notification(String recipientEmail, String subject, String message, String type) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.type = type;
        this.sentAt = LocalDateTime.now();
        this.read = false;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}