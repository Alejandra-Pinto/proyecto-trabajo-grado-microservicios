// src/main/java/com/unicauca/front/service/DocumentStorageService.java
package com.unicauca.front.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DocumentStorageService {
    
    // Ruta relativa dentro del proyecto
    private static final String BASE_DIR = "documentos";
    private static final String FORMATOS_DIR = BASE_DIR + "/formatos-a";
    private static final String CARTAS_DIR = BASE_DIR + "/cartas";
    private static final String ANTEPROYECTOS_DIR = BASE_DIR + "/anteproyectos";
    
    public DocumentStorageService() {
        // Crear directorios si no existen
        crearDirectorios();
    }
    
    private void crearDirectorios() {
        new File(FORMATOS_DIR).mkdirs();
        new File(CARTAS_DIR).mkdirs();
        new File(ANTEPROYECTOS_DIR).mkdirs();
    }
    
    /**
     * Guarda un archivo localmente y devuelve la ruta relativa
     */
    public String guardarDocumento(File archivoOrigen, Long userId, String tipo) throws IOException {
        // Generar nombre único
        String nombreUnico = generarNombreUnico(archivoOrigen, userId, tipo);
        
        // Determinar carpeta destino
        String carpetaDestino = determinarCarpeta(tipo);
        Path rutaDestino = Paths.get(carpetaDestino, nombreUnico);
        
        // Copiar archivo
        Files.copy(archivoOrigen.toPath(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);
        
        // Devolver ruta relativa (para guardar en BD)
        return determinarRutaRelativa(tipo) + "/" + nombreUnico;
    }
    
    /**
     * Obtiene un archivo desde el almacenamiento local
     */
    public File obtenerDocumento(String rutaRelativa) {
        Path rutaCompleta = Paths.get(BASE_DIR, rutaRelativa);
        return rutaCompleta.toFile();
    }
    
    /**
     * Verifica si un archivo existe
     */
    public boolean existeDocumento(String rutaRelativa) {
        Path rutaCompleta = Paths.get(BASE_DIR, rutaRelativa);
        return Files.exists(rutaCompleta);
    }
    
    private String generarNombreUnico(File archivo, Long userId, String tipo) {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        String extension = obtenerExtension(archivo.getName());
        String prefijo = obtenerPrefijoTipo(tipo);
        
        return String.format("%s_%d_%s%s", 
            timestamp, 
            userId != null ? userId : 0, 
            prefijo, 
            extension);
    }
    
    private String determinarCarpeta(String tipo) {
        switch (tipo.toUpperCase()) {
            case "FORMATO_A":
                return FORMATOS_DIR;
            case "CARTA_ACEPTACION":
                return CARTAS_DIR;
            case "ANTEPROYECTO":
                return ANTEPROYECTOS_DIR;
            default:
                return BASE_DIR;
        }
    }
    
    private String determinarRutaRelativa(String tipo) {
        switch (tipo.toUpperCase()) {
            case "FORMATO_A":
                return "formatos-a";
            case "CARTA_ACEPTACION":
                return "cartas";
            case "ANTEPROYECTO":
                return "anteproyectos";
            default:
                return "";
        }
    }
    
    private String obtenerExtension(String nombreArchivo) {
        int lastDot = nombreArchivo.lastIndexOf('.');
        return lastDot > 0 ? nombreArchivo.substring(lastDot) : "";
    }
    
    private String obtenerPrefijoTipo(String tipo) {
        switch (tipo.toUpperCase()) {
            case "FORMATO_A": return "FA";
            case "CARTA_ACEPTACION": return "CA";
            case "ANTEPROYECTO": return "AP";
            default: return "DOC";
        }
    }
}