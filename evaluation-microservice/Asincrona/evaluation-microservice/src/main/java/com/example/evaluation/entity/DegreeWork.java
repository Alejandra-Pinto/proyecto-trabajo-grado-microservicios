package com.example.evaluation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.entity.enums.EnumModalidad;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "degree_works")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DegreeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Student> estudiantes = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    private Evaluador directorProyecto;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluador> codirectoresProyecto = new ArrayList<>();

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
    @JsonManagedReference
    private List<Document> formatosA = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Document> anteproyectos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Document> cartasAceptacion = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EnumEstadoDegreeWork estado;

    private int noAprobadoCount = 0;

    @Column(length = 2000)
    private String correcciones = "";

    @ManyToOne
    private Evaluador evaluador1;

    @ManyToOne
    private Evaluador evaluador2;

    public void incrementNoAprobadoCount() {
        this.noAprobadoCount++;
    }

    public String getTituloProyecto() {
        return this.titulo;
    }
}