package com.unicauca.front.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DegreeWork {

    private Long id;
    private Student estudiante;
    private Teacher directorProyecto;
    private Teacher codirectorProyecto;
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

    //Constructor con algunos campos básicos
    public DegreeWork(Student estudiante, Teacher directorProyecto, String tituloProyecto, Modalidad modalidad) {
        this.estudiante = estudiante;
        this.directorProyecto = directorProyecto;
        this.tituloProyecto = tituloProyecto;
        this.modalidad = modalidad;
    }

    //Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getEstudiante() { return estudiante; }
    public void setEstudiante(Student estudiante) { this.estudiante = estudiante; }

    public Teacher getDirectorProyecto() { return directorProyecto; }
    public void setDirectorProyecto(Teacher directorProyecto) { this.directorProyecto = directorProyecto; }

    public Teacher getCodirectorProyecto() { return codirectorProyecto; }
    public void setCodirectorProyecto(Teacher codirectorProyecto) { this.codirectorProyecto = codirectorProyecto; }

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

