package com.example.evaluation.infra.dto;

public class EvaluationRequestDTO {

    private Long documentId;
    private String evaluadorCorreo;
    private String resultado;
    private String tipo;
    private String correcciones; // ✅ nuevo campo

    // Getters y Setters
    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getEvaluadorCorreo() {
        return evaluadorCorreo;
    }

    public void setEvaluadorCorreo(String evaluadorCorreo) {
        this.evaluadorCorreo = evaluadorCorreo;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCorrecciones() { // ✅ agregado
        return correcciones;
    }

    public void setCorrecciones(String correcciones) { // ✅ agregado
        this.correcciones = correcciones;
    }
}
