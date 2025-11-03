package com.example.evaluation.entity;

import com.example.evaluation.entity.enums.EnumEstadoDocument;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
public class Document {
    @Id
    private Long id;
    private String url;

    @Enumerated(EnumType.STRING)
    private EnumEstadoDocument estado;

    @ManyToOne
    @JsonBackReference
    private DegreeWork degreeWork;

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public EnumEstadoDocument getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoDocument estado) {
        this.estado = estado;
    }

    public DegreeWork getDegreeWork() {
        return degreeWork;
    }

    public void setDegreeWork(DegreeWork degreeWork) {
        this.degreeWork = degreeWork;
    }

}
