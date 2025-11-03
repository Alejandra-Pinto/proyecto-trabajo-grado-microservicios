package com.example.evaluation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el documento que está siendo evaluado
    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id")
    private Document document;

    // Resultado de la evaluación (ej. "APROBADO", "RECHAZADO")
    private String resultado;

    // Tipo de evaluación (ej. "PRIMERA", "SEGUNDA", "FINAL")
    private String type;

    // Fecha en la que se realizó la evaluación
    private LocalDateTime sentAt;

    // Evaluador (puede ser profesor o coordinador decorado)
    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluador_id")
    private Evaluador evaluador;

    // Constructor
    public Evaluation() {
        this.sentAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Evaluador getEvaluador() {
        return evaluador;
    }

    public void setEvaluador(Evaluador evaluador) {
        this.evaluador = evaluador;
    }
}
