package com.unicauca.front.dto;

public class EvaluationRequestDTO {
    private Long documentId;
    private String evaluadorEmail;  // Cambiar de evaluadorId a evaluadorEmail
    private String resultado;
    private String tipo = "FORMATO_A";
    private String correcciones;

    // Getters y Setters
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public String getEvaluadorEmail() { return evaluadorEmail; }
    public void setEvaluadorEmail(String evaluadorEmail) { this.evaluadorEmail = evaluadorEmail; }
    
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getCorrecciones() { return correcciones; }
    public void setCorrecciones(String correcciones) { this.correcciones = correcciones; }
}