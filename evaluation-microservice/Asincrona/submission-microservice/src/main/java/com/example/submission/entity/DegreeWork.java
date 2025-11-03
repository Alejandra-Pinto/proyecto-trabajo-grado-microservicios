package com.example.submission.entity;

import com.example.submission.entity.enums.EstadoDegreeWork;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class DegreeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String modality;  // Ej: “Investigación”, “Aplicativo”, etc.
    private String creationDate;
    private String generalObjective;

    @ElementCollection
    private List<String> specificObjectives;

    @Enumerated(EnumType.STRING)
    private EstadoDegreeWork estadoP;

    // Relaciones con profesores y estudiantes
    @ManyToOne
    @JoinColumn(name = "student1_id")
    private Student student1;

    @ManyToOne
    @JoinColumn(name = "student2_id", nullable = true)
    private Student student2;

    @ManyToOne
    @JoinColumn(name = "director_id")
    private Professor director;

    @ManyToOne
    @JoinColumn(name = "co_director1_id")
    private Professor coDirector1;

    @ManyToOne
    @JoinColumn(name = "co_director2_id", nullable = true)
    private Professor coDirector2;

    @OneToMany(mappedBy = "degreeWork", cascade = CascadeType.ALL)
    private List<Document> documentos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getGeneralObjective() { return generalObjective; }
    public void setGeneralObjective(String generalObjective) { this.generalObjective = generalObjective; }

    public List<String> getSpecificObjectives() { return specificObjectives; }
    public void setSpecificObjectives(List<String> specificObjectives) { this.specificObjectives = specificObjectives; }

    public EstadoDegreeWork getEstadoP() { return estadoP; }
    public void setEstadoP(EstadoDegreeWork estadoP) { this.estadoP = estadoP; }

    public Student getStudent1() { return student1; }
    public void setStudent1(Student student1) { this.student1 = student1; }

    public Student getStudent2() { return student2; }
    public void setStudent2(Student student2) { this.student2 = student2; }

    public Professor getDirector() { return director; }
    public void setDirector(Professor director) { this.director = director; }

    public Professor getCoDirector1() { return coDirector1; }
    public void setCoDirector1(Professor coDirector1) { this.coDirector1 = coDirector1; }

    public Professor getCoDirector2() { return coDirector2; }
    public void setCoDirector2(Professor coDirector2) { this.coDirector2 = coDirector2; }

    public List<Document> getDocumentos() { return documentos; }
    public void setDocumentos(List<Document> documentos) { this.documentos = documentos; }
}
