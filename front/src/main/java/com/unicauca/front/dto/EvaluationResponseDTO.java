package com.unicauca.front.dto;

import java.time.LocalDateTime;

public class EvaluationResponseDTO {
    private Long id;
    private Long documentId;
    private String resultado;
    private String tipo;
    private LocalDateTime fechaEvaluacion;
    private String evaluadorNombre;
    private String evaluadorRol;
    private String evaluadorCorreo;
    private String correcciones;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public LocalDateTime getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }
    
    public String getEvaluadorNombre() { return evaluadorNombre; }
    public void setEvaluadorNombre(String evaluadorNombre) { this.evaluadorNombre = evaluadorNombre; }
    
    public String getEvaluadorRol() { return evaluadorRol; }
    public void setEvaluadorRol(String evaluadorRol) { this.evaluadorRol = evaluadorRol; }
    
    public String getEvaluadorCorreo() { return evaluadorCorreo; }
    public void setEvaluadorCorreo(String evaluadorCorreo) { this.evaluadorCorreo = evaluadorCorreo; }
    
    public String getCorrecciones() { return correcciones; }
    public void setCorrecciones(String correcciones) { this.correcciones = correcciones; }
}