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
}
