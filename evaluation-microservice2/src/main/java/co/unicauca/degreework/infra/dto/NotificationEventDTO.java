package co.unicauca.degreework.infra.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationEventDTO implements Serializable {
    private String eventType;     // Ej: FORMATO_A_SUBIDO, FORMATO_A_EVALUADO, ANTEPROYECTO_SUBIDO, EVALUADORES_ASIGNADOS
    private String title;         // Título del trabajo de grado
    private String modality;      // Modalidad (opcional)
    private List<String> recipientEmails; // Emails específicos (opcional)
    private String targetRole;    // Rol para notificar (COORDINATOR, PROFESSOR, etc.)
    private String directorEmail;
    private String coDirector1Email;
    private String coDirector2Email;
    private List<String> evaluatorEmails; // Para cuando se asignen evaluadores
    private String status;        // Aprobado, Rechazado, etc.
    private String observations;  // Observaciones de la evaluación
    private Integer attemptNumber; // Número de intento (para Formato A)
    private LocalDateTime timestamp;

    // Constructor vacío
    public NotificationEventDTO() {}

    // Getters y setters para todos los campos
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public List<String> getRecipientEmails() { return recipientEmails; }
    public void setRecipientEmails(List<String> recipientEmails) { this.recipientEmails = recipientEmails; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getDirectorEmail() { return directorEmail; }
    public void setDirectorEmail(String directorEmail) { this.directorEmail = directorEmail; }

    public String getCoDirector1Email() { return coDirector1Email; }
    public void setCoDirector1Email(String coDirector1Email) { this.coDirector1Email = coDirector1Email; }

    public String getCoDirector2Email() { return coDirector2Email; }
    public void setCoDirector2Email(String coDirector2Email) { this.coDirector2Email = coDirector2Email; }

    public List<String> getEvaluatorEmails() { return evaluatorEmails; }
    public void setEvaluatorEmails(List<String> evaluatorEmails) { this.evaluatorEmails = evaluatorEmails; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}