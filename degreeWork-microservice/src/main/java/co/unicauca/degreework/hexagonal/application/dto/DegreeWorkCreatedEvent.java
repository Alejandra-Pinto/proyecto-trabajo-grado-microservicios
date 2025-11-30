package co.unicauca.degreework.hexagonal.application.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Evento que se envía a RabbitMQ cuando se crea un nuevo trabajo de grado.
 * Incluye los datos generales y los documentos asociados.
 */
public class DegreeWorkCreatedEvent implements Serializable {

    // Información general
    private Long id;
    private String titulo;
    private String modalidad;
    private String directorEmail;
    private List<String> estudiantesEmails;
    private List<String> codirectoresEmails;
    private LocalDate fechaActual;
    private String estado;

    // Documentos asociados
    private List<DocumentDTO> formatosA;
    private List<DocumentDTO> anteproyectos;
    private List<DocumentDTO> cartasAceptacion;

    public DegreeWorkCreatedEvent() {
    }

    public DegreeWorkCreatedEvent(Long id, String titulo, String modalidad, String directorEmail,
                                  List<String> estudiantesEmails, List<String> codirectoresEmails,
                                  LocalDate fechaActual, String estado,
                                  List<DocumentDTO> formatosA, List<DocumentDTO> anteproyectos,
                                  List<DocumentDTO> cartasAceptacion) {
        this.id = id;
        this.titulo = titulo;
        this.modalidad = modalidad;
        this.directorEmail = directorEmail;
        this.estudiantesEmails = estudiantesEmails;
        this.codirectoresEmails = codirectoresEmails;
        this.fechaActual = fechaActual;
        this.estado = estado;
        this.formatosA = formatosA;
        this.anteproyectos = anteproyectos;
        this.cartasAceptacion = cartasAceptacion;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getDirectorEmail() { return directorEmail; }
    public void setDirectorEmail(String directorEmail) { this.directorEmail = directorEmail; }

    public List<String> getEstudiantesEmails() { return estudiantesEmails; }
    public void setEstudiantesEmails(List<String> estudiantesEmails) { this.estudiantesEmails = estudiantesEmails; }

    public List<String> getCodirectoresEmails() { return codirectoresEmails; }
    public void setCodirectoresEmails(List<String> codirectoresEmails) { this.codirectoresEmails = codirectoresEmails; }

    public LocalDate getFechaActual() { return fechaActual; }
    public void setFechaActual(LocalDate fechaActual) { this.fechaActual = fechaActual; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<DocumentDTO> getFormatosA() { return formatosA; }
    public void setFormatosA(List<DocumentDTO> formatosA) { this.formatosA = formatosA; }

    public List<DocumentDTO> getAnteproyectos() { return anteproyectos; }
    public void setAnteproyectos(List<DocumentDTO> anteproyectos) { this.anteproyectos = anteproyectos; }

    public List<DocumentDTO> getCartasAceptacion() { return cartasAceptacion; }
    public void setCartasAceptacion(List<DocumentDTO> cartasAceptacion) { this.cartasAceptacion = cartasAceptacion; }
}
