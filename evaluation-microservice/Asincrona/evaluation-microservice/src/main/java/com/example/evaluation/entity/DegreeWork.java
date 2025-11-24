package com.example.evaluation.entity;

import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.entity.enums.EnumModalidad;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "degree_works")
public class DegreeWork {

    @Id
    private Long id;

    @ElementCollection
    @CollectionTable(name = "degreework_estudiantes_emails", joinColumns = @JoinColumn(name = "degree_work_id"))
    @Column(name = "email")
    private List<String> estudiantesEmails = new ArrayList<>();

    private String directorEmail;

    @ElementCollection
    @CollectionTable(name = "degreework_codirectores_emails", joinColumns = @JoinColumn(name = "degree_work_id"))
    @Column(name = "email")
    private List<String> codirectoresEmails = new ArrayList<>();

    private String titulo;

    @Enumerated(EnumType.STRING)
    private EnumModalidad modalidad;

    private LocalDate fechaActual;

    private String objetivoGeneral;

    @ElementCollection
    @CollectionTable(name = "degreework_objetivos_especificos", joinColumns = @JoinColumn(name = "degree_work_id"))
    @Column(name = "objetivo")
    private List<String> objetivosEspecificos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> formatosA = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> anteproyectos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> cartasAceptacion = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EnumEstadoDegreeWork estado;

    private String calificacion;

    private int noAprobadoCount = 0;

    @Column(length = 2000)
    private String correcciones = "";

    public void incrementNoAprobadoCount() {
        this.noAprobadoCount++;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public EnumModalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(EnumModalidad modalidad) {
        this.modalidad = modalidad;
    }

    public LocalDate getFechaActual() {
        return fechaActual;
    }

    public void setFechaActual(LocalDate fechaActual) {
        this.fechaActual = fechaActual;
    }

    public String getObjetivoGeneral() {
        return objetivoGeneral;
    }

    public void setObjetivoGeneral(String objetivoGeneral) {
        this.objetivoGeneral = objetivoGeneral;
    }

    public List<String> getObjetivosEspecificos() {
        return objetivosEspecificos;
    }

    public void setObjetivosEspecificos(List<String> objetivosEspecificos) {
        this.objetivosEspecificos = objetivosEspecificos;
    }

    public List<Document> getFormatosA() {
        return formatosA;
    }

    public void setFormatosA(List<Document> formatosA) {
        this.formatosA = formatosA;
    }

    public List<Document> getAnteproyectos() {
        return anteproyectos;
    }

    public void setAnteproyectos(List<Document> anteproyectos) {
        this.anteproyectos = anteproyectos;
    }

    public List<Document> getCartasAceptacion() {
        return cartasAceptacion;
    }

    public void setCartasAceptacion(List<Document> cartasAceptacion) {
        this.cartasAceptacion = cartasAceptacion;
    }

    public EnumEstadoDegreeWork getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoDegreeWork estado) {
        this.estado = estado;
    }

    public int getNoAprobadoCount() {
        return noAprobadoCount;
    }

    public void setNoAprobadoCount(int noAprobadoCount) {
        this.noAprobadoCount = noAprobadoCount;
    }

    public String getCorrecciones() {
        return correcciones;
    }

    public void setCorrecciones(String correcciones) {
        this.correcciones = correcciones;
    }

    public List<String> getEstudiantesEmails() {
        return estudiantesEmails;
    }

    public void setEstudiantesEmails(List<String> estudiantesEmails) {
        this.estudiantesEmails = estudiantesEmails;
    }

    public String getDirectorEmail() {
        return directorEmail;
    }

    public void setDirectorEmail(String directorEmail) {
        this.directorEmail = directorEmail;
    }

    public List<String> getCodirectoresEmails() {
        return codirectoresEmails;
    }

    public void setCodirectoresEmails(List<String> codirectoresEmails) {
        this.codirectoresEmails = codirectoresEmails;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(String calificacion) {
        this.calificacion = calificacion;
    }
}
