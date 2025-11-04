package com.unicauca.front.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DegreeWork {

    private Long id;
    private List<User> estudiantes = new ArrayList<>();
    private User directorProyecto;
    private List<User> codirectoresProyecto = new ArrayList<>();
    private String tituloProyecto;
    private Modalidad modalidad;
    private LocalDate fechaActual;
    private String objetivoGeneral;
    private List<String> objetivosEspecificos;
    
    //listas de documentos
    private List<Document> formatosA = new ArrayList<>();
    private List<Document> anteproyectos = new ArrayList<>();
    private List<Document> cartasAceptacion = new ArrayList<>();

    private EnumEstadoDegreeWork estado;
    private String correcciones;
    private int noAprobadoCount;

    //Constructor vacío necesario para frameworks como JSON, Jackson
    public DegreeWork() {}

    //Constructor con algunos campos básicos - MANTENIENDO LA FIRMA ORIGINAL
    public DegreeWork(Student estudiante, Teacher directorProyecto, String tituloProyecto, Modalidad modalidad) {
        // Como Student y Teacher heredan de User, podemos usarlos directamente
        if (estudiante != null) {
            this.estudiantes.add(estudiante); // Student ES-UN User
        }
        
        // Teacher también ES-UN User
        this.directorProyecto = directorProyecto;
        
        this.tituloProyecto = tituloProyecto;
        this.modalidad = modalidad;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // Getters y Setters actualizados
    public List<User> getEstudiantes() { return estudiantes; }
    public void setEstudiantes(List<User> estudiantes) { this.estudiantes = estudiantes; }

    public User getDirectorProyecto() { return directorProyecto; }
    public void setDirectorProyecto(User directorProyecto) { this.directorProyecto = directorProyecto; }

    public List<User> getCodirectoresProyecto() { return codirectoresProyecto; }
    public void setCodirectoresProyecto(List<User> codirectoresProyecto) { this.codirectoresProyecto = codirectoresProyecto; }

    // Método helper para obtener el primer estudiante
    public User getPrimerEstudiante() {
        return estudiantes != null && !estudiantes.isEmpty() ? estudiantes.get(0) : null;
    }
    
    // MÉTODO DE COMPATIBILIDAD - Para no romper código existente que use getEstudiante()
    public User getEstudiante() {
        return getPrimerEstudiante();
    }
    
    // MÉTODO DE COMPATIBILIDAD - Para no romper código existente que use setEstudiante()
    public void setEstudiante(User estudiante) {
        if (this.estudiantes == null) {
            this.estudiantes = new ArrayList<>();
        } else {
            this.estudiantes.clear();
        }
        if (estudiante != null) {
            this.estudiantes.add(estudiante);
        }
    }
    
    // MÉTODO DE COMPATIBILIDAD - Para código que espera un codirector individual
    public User getCodirectorProyecto() {
        return codirectoresProyecto != null && !codirectoresProyecto.isEmpty() ? 
               codirectoresProyecto.get(0) : null;
    }
    
    // MÉTODO DE COMPATIBILIDAD - Para código que establece un codirector individual
    public void setCodirectorProyecto(User codirector) {
        if (this.codirectoresProyecto == null) {
            this.codirectoresProyecto = new ArrayList<>();
        } else {
            this.codirectoresProyecto.clear();
        }
        if (codirector != null) {
            this.codirectoresProyecto.add(codirector);
        }
    }

    public String getTituloProyecto() { return tituloProyecto; }
    public void setTituloProyecto(String tituloProyecto) { this.tituloProyecto = tituloProyecto; }

    public Modalidad getModalidad() { return modalidad; }
    public void setModalidad(Modalidad modalidad) { this.modalidad = modalidad; }

    public LocalDate getFechaActual() { return fechaActual; }
    public void setFechaActual(LocalDate fechaActual) { this.fechaActual = fechaActual; }

    public String getObjetivoGeneral() { return objetivoGeneral; }
    public void setObjetivoGeneral(String objetivoGeneral) { this.objetivoGeneral = objetivoGeneral; }

    public List<String> getObjetivosEspecificos() { return objetivosEspecificos; }
    public void setObjetivosEspecificos(List<String> objetivosEspecificos) { this.objetivosEspecificos = objetivosEspecificos; }

    public EnumEstadoDegreeWork getEstado() { return estado; }
    public void setEstado(EnumEstadoDegreeWork estado) { this.estado = estado; }

    public String getCorrecciones() { return correcciones; }
    public void setCorrecciones(String correcciones) { this.correcciones = correcciones; }

    public int getNoAprobadoCount() { return noAprobadoCount; }
    public void setNoAprobadoCount(int noAprobadoCount) { this.noAprobadoCount = noAprobadoCount; }

    // Getters y Setters para documentos
    public List<Document> getFormatosA() { return formatosA; }
    public void setFormatosA(List<Document> formatosA) { this.formatosA = formatosA; }

    public List<Document> getAnteproyectos() { return anteproyectos; }
    public void setAnteproyectos(List<Document> anteproyectos) { this.anteproyectos = anteproyectos; }

    public List<Document> getCartasAceptacion() { return cartasAceptacion; }
    public void setCartasAceptacion(List<Document> cartasAceptacion) { this.cartasAceptacion = cartasAceptacion; }
    
    @Override
    public String toString() {
        return "DegreeWork{" +
                "id=" + id +
                ", tituloProyecto='" + tituloProyecto + '\'' +
                ", modalidad=" + modalidad +
                ", estado=" + estado +
                '}';
    }
}