package co.unicauca.degreework.hexagonal.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import co.unicauca.degreework.hexagonal.domain.model.enums.*;
import co.unicauca.degreework.hexagonal.domain.vo.*;


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

    @Embedded
    private Titulo titulo;

    @Enumerated(EnumType.STRING)
    private EnumModalidad modalidad;

    @Embedded
    private FechaCreacion fechaActual;

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
    private int noAprobadoCountAnteproyecto = 0;

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
    public void incrementNoAprobadoCountAnteproyecto() {
        this.noAprobadoCountAnteproyecto++;
    }

    public void resetNoAprobadoCountAnteproyecto() {
        this.noAprobadoCountAnteproyecto = 0;
    }
    public void resetNoAprobadoCountFormatoA() {
        this.noAprobadoCount = 0;
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

    @JsonGetter("titulo")
    public String getTituloAsString() {
        return titulo != null ? titulo.getValor() : null;
    }

    @JsonSetter("titulo")
    public void setTituloFromString(String tituloStr) {
        if (tituloStr != null && !tituloStr.trim().isEmpty()) {
            this.titulo = new Titulo(tituloStr);
        }
    }

    @JsonGetter("fechaActual")
    public String getFechaActualAsString() {
        return fechaActual != null ? fechaActual.getValor().toString() : null;
    }

    @JsonSetter("fechaActual")
    public void setFechaActualFromString(String fechaStr) {
        if (fechaStr != null && !fechaStr.trim().isEmpty()) {
            LocalDate fecha = LocalDate.parse(fechaStr);
            this.fechaActual = new FechaCreacion(fecha);
        }
    }

    public EnumModalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(EnumModalidad modalidad) {
        this.modalidad = modalidad;
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

    public int getNoAprobadoCountPorTipo(EnumTipoDocumento tipo) {
        return switch (tipo) {
            case FORMATO_A -> this.noAprobadoCount;
            case ANTEPROYECTO -> this.noAprobadoCountAnteproyecto;
            default -> 0;
        };
    }

    public void manejarRevision(Document documento) {
        if (documento == null) {
            throw new IllegalArgumentException("El documento no puede ser nulo.");
        }
        
        // Solo manejar si está en NO_ACEPTADO
        if (documento.getEstado() != EnumEstadoDocument.NO_ACEPTADO) {
            return;
        }

        // Incrementar el contador correspondiente
        switch (documento.getTipo()) {
            case FORMATO_A -> incrementNoAprobadoCount();
            case ANTEPROYECTO -> incrementNoAprobadoCountAnteproyecto();
        }
        
        // Obtener el contador actual para este tipo
        int contador = getNoAprobadoCountPorTipo(documento.getTipo());
        
        System.out.println("📊 Contador actual para " + documento.getTipo() + ": " + contador);
        
        // Determinar el nuevo estado basado en el contador
        switch (contador) {
            case 1 -> {
                documento.setEstado(EnumEstadoDocument.SEGUNDA_REVISION);
                System.out.println("🔄 Estado cambiado a SEGUNDA_REVISION");
            }
            case 2 -> {
                documento.setEstado(EnumEstadoDocument.TERCERA_REVISION);
                System.out.println("🔄 Estado cambiado a TERCERA_REVISION");
            }
            case 3 -> {
                documento.setEstado(EnumEstadoDocument.RECHAZADO);
                System.out.println("❌ Estado cambiado a RECHAZADO definitivo");
            }
            default -> {
                documento.setEstado(EnumEstadoDocument.RECHAZADO);
                System.out.println("❌ Estado cambiado a RECHAZADO (máximo de intentos)");
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
        
        // Resetear contador de anteproyecto al empezar nuevo tipo
        resetNoAprobadoCountAnteproyecto();
        
        if (this.anteproyectos == null) {
            this.anteproyectos = new ArrayList<>();
        }
        this.anteproyectos.add(anteproyecto);

        // Actualizar estado del trabajo de grado
        this.estado = EnumEstadoDegreeWork.ANTEPROYECTO;
    }

    public void agregarFormatoA(Document formatoA) {
        if (formatoA.getRutaArchivo() == null || formatoA.getRutaArchivo().isBlank()) {
            throw new IllegalArgumentException("El Formato A debe tener una ruta de archivo válida.");
        }

        formatoA.setTipo(EnumTipoDocumento.FORMATO_A);
        formatoA.setFechaActual(LocalDate.now());
        
        // VERIFICAR SI ES PRIMER FORMATO A
        boolean esPrimerFormatoA = this.formatosA == null || this.formatosA.isEmpty();
        
        if (esPrimerFormatoA) {
            // Primer Formato A: estado inicial PRIMERA_REVISION
            formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
            // Resetear contador solo si es el PRIMERO
            resetNoAprobadoCountFormatoA();
        } else {
            // NO ES EL PRIMERO: Obtener estado del último formato
            Document ultimoFormatoA = getUltimoDocumentoPorTipo(EnumTipoDocumento.FORMATO_A);
            
            if (ultimoFormatoA != null) {
                // Si el último está ACEPTADO o RECHAZADO, no se puede crear nuevo
                if (ultimoFormatoA.getEstado() == EnumEstadoDocument.ACEPTADO ||
                    ultimoFormatoA.getEstado() == EnumEstadoDocument.RECHAZADO) {
                    throw new IllegalStateException(
                        "No se puede crear nueva versión de Formato A. Estado actual: " + 
                        ultimoFormatoA.getEstado()
                    );
                }
                
                // Mantener el estado actual del último formato para la nueva versión
                formatoA.setEstado(ultimoFormatoA.getEstado());
            } else {
                formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
            }
        }
        
        if (this.formatosA == null) {
            this.formatosA = new ArrayList<>();
        }
        this.formatosA.add(formatoA);
    }

    public void documentoAceptado(EnumTipoDocumento tipo) {
        switch (tipo) {
            case FORMATO_A -> resetNoAprobadoCountFormatoA();
            case ANTEPROYECTO -> resetNoAprobadoCountAnteproyecto();
            default -> {}
        }
        System.out.println("✅ Contador reseteado para " + tipo + " al ser aceptado");
    }
}
