package com.example.notification.infra.config.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class NotificationEventDTO implements Serializable {
    private String eventType;     // Ej: FORMATO_A_SUBIDO, ANTEPROYECTO_SUBIDO, etc.
    private String title;         // Título del trabajo de grado
    private String modality;      // Modalidad (opcional)
    private String recipientEmail; // Email principal (opcional)
    private String coordinatorEmail;
    private String directorEmail;
    private String coDirector1Email;
    private String coDirector2Email;
    private LocalDateTime timestamp;

    // Constructor vacío (requerido por Jackson)
    public NotificationEventDTO() {}

    // Getters y setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getCoordinatorEmail() { return coordinatorEmail; }
    public void setCoordinatorEmail(String coordinatorEmail) { this.coordinatorEmail = coordinatorEmail; }

    public String getDirectorEmail() { return directorEmail; }
    public void setDirectorEmail(String directorEmail) { this.directorEmail = directorEmail; }

    public String getCoDirector1Email() { return coDirector1Email; }
    public void setCoDirector1Email(String coDirector1Email) { this.coDirector1Email = coDirector1Email; }

    public String getCoDirector2Email() { return coDirector2Email; }
    public void setCoDirector2Email(String coDirector2Email) { this.coDirector2Email = coDirector2Email; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
