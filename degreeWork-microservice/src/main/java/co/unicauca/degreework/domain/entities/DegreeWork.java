package co.unicauca.degreework.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;
import co.unicauca.degreework.domain.entities.enums.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "degree_works")
public class DegreeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Un trabajo de grado puede tener uno o dos estudiantes
     * (solo en modalidad de investigación).
     */
    @ManyToMany
    @JoinTable(
        name = "degreework_estudiantes",
        joinColumns = @JoinColumn(name = "degree_work_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> estudiantes = new ArrayList<>();

    /**
     * Un solo director.
     */
    @ManyToOne
    private User directorProyecto;

    /**
     * Puede tener cero, uno o dos codirectores.
     */
    @ManyToMany
    @JoinTable(
        name = "degreework_codirectores",
        joinColumns = @JoinColumn(name = "degree_work_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> codirectoresProyecto = new ArrayList<>();

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

    public List<User> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<User> estudiantes) {
        this.estudiantes = estudiantes;
    }

    public User getDirectorProyecto() {
        return directorProyecto;
    }

    public void setDirectorProyecto(User directorProyecto) {
        this.directorProyecto = directorProyecto;
    }

    public List<User> getCodirectoresProyecto() {
        return codirectoresProyecto;
    }

    public void setCodirectoresProyecto(List<User> codirectoresProyecto) {
        this.codirectoresProyecto = codirectoresProyecto;
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
}
