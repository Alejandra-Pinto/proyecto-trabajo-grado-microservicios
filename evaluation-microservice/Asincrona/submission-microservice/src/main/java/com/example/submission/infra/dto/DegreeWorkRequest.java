package com.example.submission.infra.dto;

import com.example.submission.entity.enums.EstadoDegreeWork;
import java.util.List;

public class DegreeWorkRequest {
    private String title;
    private String modality;
    private String creationDate;
    private String generalObjective;
    private List<String> specificObjectives;
    private EstadoDegreeWork estadoP;

    private Long student1Id;
    private Long student2Id;
    private Long directorId;
    private Long coDirector1Id;
    private Long coDirector2Id;

    // getters y setters
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

    public Long getStudent1Id() { return student1Id; }
    public void setStudent1Id(Long student1Id) { this.student1Id = student1Id; }

    public Long getStudent2Id() { return student2Id; }
    public void setStudent2Id(Long student2Id) { this.student2Id = student2Id; }

    public Long getDirectorId() { return directorId; }
    public void setDirectorId(Long directorId) { this.directorId = directorId; }

    public Long getCoDirector1Id() { return coDirector1Id; }
    public void setCoDirector1Id(Long coDirector1Id) { this.coDirector1Id = coDirector1Id; }

    public Long getCoDirector2Id() { return coDirector2Id; }
    public void setCoDirector2Id(Long coDirector2Id) { this.coDirector2Id = coDirector2Id; }
}

