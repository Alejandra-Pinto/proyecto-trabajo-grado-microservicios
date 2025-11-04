package com.example.evaluation.infra.messaging;

import com.example.evaluation.entity.*;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.entity.enums.EnumModalidad;
import com.example.evaluation.infra.dto.DegreeWorkCreatedEvent;
import com.example.evaluation.infra.dto.DocumentDTO;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Escucha los eventos de creación de trabajos de grado desde el microservicio de proyectos.
 * Cuando recibe el evento, lo guarda en la base de datos local del microservicio de evaluación.
 */
@Component
public class DegreeWorkCreatedListener {

    private final DegreeWorkRepository degreeWorkRepository;
    private final UserRepository userRepository;

    public DegreeWorkCreatedListener(DegreeWorkRepository degreeWorkRepository, UserRepository userRepository) {
        this.degreeWorkRepository = degreeWorkRepository;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = "degreework.queue")
    public void onDegreeWorkCreated(DegreeWorkCreatedEvent event) {
        System.out.println("📩 Recibido DegreeWorkCreatedEvent desde Proyectos: " + event.getTitulo() + " (ID: " + event.getId() + ")");

        // Convertir modalidad String → EnumModalidad
        EnumModalidad modalidadEnum;
        try {
            modalidadEnum = EnumModalidad.valueOf(event.getModalidad().trim().toUpperCase());
        } catch (Exception e) {
            System.err.println("⚠️ Modalidad no válida: " + event.getModalidad());
            modalidadEnum = EnumModalidad.INVESTIGACION;
        }

        // Convertir estado String → EnumEstadoDegreeWork
        EnumEstadoDegreeWork estadoEnum;
        try {
            estadoEnum = EnumEstadoDegreeWork.valueOf(event.getEstado().trim().toUpperCase());
        } catch (Exception e) {
            System.err.println("⚠️ Estado no válido: " + event.getEstado());
            estadoEnum = EnumEstadoDegreeWork.FORMATO_A;
        }

        // Buscar director
        User director = userRepository.findByEmail(event.getDirectorEmail()).orElse(null);

        // Buscar estudiantes
        List<User> estudiantes = event.getEstudiantesEmails().stream()
                .map(email -> userRepository.findByEmail(email).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toList());

        // Buscar codirectores
        List<User> codirectores = event.getCodirectoresEmails().stream()
                .map(email -> userRepository.findByEmail(email).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toList());

        // Documentos asociados
        List<Document> formatosA = convertirDocumentos(event.getFormatosA());
        List<Document> anteproyectos = convertirDocumentos(event.getAnteproyectos());
        List<Document> cartas = convertirDocumentos(event.getCartasAceptacion());

        // Crear y guardar el DegreeWork localmente
        DegreeWork degreeWork = DegreeWork.builder()
                .id(event.getId()) // 👈 Se asigna el mismo id del microservicio de proyectos
                .titulo(event.getTitulo())
                .modalidad(modalidadEnum)
                .directorProyecto(director)
                .estudiantes(estudiantes)
                .codirectoresProyecto(codirectores)
                .fechaActual(event.getFechaActual())
                .estado(estadoEnum)
                .formatosA(formatosA)
                .anteproyectos(anteproyectos)
                .cartasAceptacion(cartas)
                .correcciones("")
                .noAprobadoCount(0)
                .build();

        degreeWorkRepository.save(degreeWork);
        System.out.println("✅ Trabajo de grado guardado en evaluación: " + degreeWork.getTitulo() + " (ID: " + degreeWork.getId() + ")");
    }

    /**
     * Convierte una lista de DocumentDTO a una lista de entidades Document.
     */
    private List<Document> convertirDocumentos(List<DocumentDTO> dtos) {
        if (dtos == null) return List.of();

        return dtos.stream().map(dto -> {
            Document doc = new Document();
            doc.setId(dto.getId()); // 👈 Guarda también el id del documento
            doc.setRutaArchivo(dto.getRutaArchivo());
            doc.setEstado(dto.getEstado());
            doc.setTipo(dto.getTipo());
            return doc;
        }).collect(Collectors.toList());
    }
}
