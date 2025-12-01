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

    @ManyToMany
    @JoinTable(
        name = "degreework_evaluadores",
        joinColumns = @JoinColumn(name = "degree_work_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> evaluadores = new ArrayList<>();

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

    public void setEvaluadores(List<User> evaluadores) {
        this.evaluadores = evaluadores;
    }

    public List<User> getEvaluadores() {
        return evaluadores;
    }

    public void agregarAnteproyecto(Document anteproyecto) {
        if (formatosA == null || formatosA.isEmpty()) {
            throw new IllegalStateException("No se puede agregar un anteproyecto sin haber registrado un Formato A.");
        }

        // Obtener el último Formato A
        Document ultimoFormatoA = formatosA.get(formatosA.size() - 1);

        if (ultimoFormatoA.getEstado() != EnumEstadoDocument.ACEPTADO) {
            throw new IllegalStateException("El último Formato A debe estar en estado ACEPTADO para poder agregar un anteproyecto.");
        }

        if (anteproyecto.getRutaArchivo() == null || anteproyecto.getRutaArchivo().isBlank()) {
            throw new IllegalArgumentException("El anteproyecto debe tener una ruta de archivo válida.");
        }

        anteproyecto.setTipo(EnumTipoDocumento.ANTEPROYECTO);
        anteproyecto.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        anteproyecto.setFechaActual(LocalDate.now());
        this.anteproyectos.add(anteproyecto);

        // Actualizar estado del trabajo de grado
        this.estado = EnumEstadoDegreeWork.ANTEPROYECTO;
    }

    public void manejarRevision(Document documento) {
        if (documento == null) {
            throw new IllegalArgumentException("El documento no puede ser nulo.");
        }

        // Si es un documento nuevo (sin ID o sin estado previo), se asume en primera revisión
        if (documento.getId() == null || documento.getEstado() == null) {
            documento.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
            return;
        }

        // Bloquear solo si ya está en estado final
        if (documento.getEstado() == EnumEstadoDocument.ACEPTADO ||
            documento.getEstado() == EnumEstadoDocument.RECHAZADO) {
            return;
        }

        // Manejo de revisiones no aceptadas
        if (documento.getEstado() == EnumEstadoDocument.NO_ACEPTADO) {
            incrementNoAprobadoCount();

            switch (noAprobadoCount) {
                case 1 -> documento.setEstado(EnumEstadoDocument.SEGUNDA_REVISION);
                case 2 -> documento.setEstado(EnumEstadoDocument.TERCERA_REVISION);
                case 3 -> documento.setEstado(EnumEstadoDocument.RECHAZADO);
                default -> documento.setEstado(EnumEstadoDocument.RECHAZADO);
            }
        }
    }

    public Document getUltimoDocumentoPorTipo(EnumTipoDocumento tipo) {
        List<Document> lista = switch (tipo) {
            case FORMATO_A -> this.getFormatosA();
            case ANTEPROYECTO -> this.getAnteproyectos();
            case CARTA_ACEPTACION -> this.getCartasAceptacion();
            default -> throw new IllegalArgumentException("Tipo de documento no reconocido: " + tipo);
        };
        if (lista == null || lista.isEmpty()) return null;
        return lista.get(lista.size() - 1);
    }

        /**
     * Actualiza solo las observaciones del trabajo de grado
     */
    public void actualizarObservaciones(String nuevasObservaciones) {
        if (nuevasObservaciones != null) {
            this.correcciones = nuevasObservaciones;
        }
    }

    /**
     * Actualiza solo el estado del trabajo de grado
     */
    public void actualizarEstado(EnumEstadoDegreeWork nuevoEstado) {
        if (nuevoEstado != null) {
            this.estado = nuevoEstado;
        }
    }

    /**
     * Actualiza tanto observaciones como estado en una sola operación
     */
    public void actualizarEvaluacion(String observaciones, EnumEstadoDegreeWork nuevoEstado) {
        if (observaciones != null) {
            this.correcciones = observaciones;
        }
        if (nuevoEstado != null) {
            this.estado = nuevoEstado;
        }
    }
}
