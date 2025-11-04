package com.example.evaluation.service;

import com.example.evaluation.entity.Document;
import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.Evaluation;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.infra.dto.DegreeWorkUpdateDTO;
import com.example.evaluation.infra.messaging.EvaluationPublisher;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.DocumentRepository;
import com.example.evaluation.repository.EvaluationRepository;
import com.example.evaluation.repository.EvaluadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvaluacionService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EvaluadorRepository evaluadorRepository;

    @Autowired
    private EvaluationPublisher evaluationPublisher;

    @Autowired
    private DegreeWorkRepository degreeWorkRepository;

    // ✅ Crear evaluación y enviar correcciones a la cola
    public Evaluation crearEvaluacion(
            Long documentId,
            String evaluadorCorreo,
            String resultado,
            String tipo,
            String correcciones) {

        // Buscar documento
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + documentId));

        // Buscar evaluador
        var evaluador = evaluadorRepository.findByCorreo(evaluadorCorreo)
                .orElseThrow(() -> new RuntimeException("Evaluador no encontrado con correo: " + evaluadorCorreo));

        // Buscar DegreeWork que contiene el documento
        DegreeWork degreeWork = degreeWorkRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("No se encontró el trabajo de grado asociado al documento ID: " + documentId));

        // Crear evaluación
        Evaluation evaluacion = new Evaluation();
        evaluacion.setDocument(document);
        evaluacion.setEvaluador(evaluador);
        evaluacion.setResultado(resultado);
        evaluacion.setType(tipo);
        evaluacion.setSentAt(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(evaluacion);

        // Mapear tipo de evaluación a estado del trabajo
        EnumEstadoDegreeWork estadoEnum = switch (tipo.toUpperCase()) {
            case "FORMATO_A" -> EnumEstadoDegreeWork.FORMATO_A;
            case "ANTEPROYECTO" -> EnumEstadoDegreeWork.ANTEPROYECTO;
            case "MONOGRAFIA" -> EnumEstadoDegreeWork.MONOGRAFIA;
            default -> throw new RuntimeException("Tipo de evaluación desconocido: " + tipo);
        };

        // Crear DTO para enviar por RabbitMQ
        DegreeWorkUpdateDTO updateDTO = DegreeWorkUpdateDTO.builder()
            .degreeWorkId(degreeWork.getId().intValue()) 
            .estado(estadoEnum.name())
            .correcciones(correcciones)
            .build();

        evaluationPublisher.publicarActualizacionDegreeWork(updateDTO);

        return saved;
    }

    // ✅ Listar todas las evaluaciones
    public List<Evaluation> listarEvaluaciones() {
        return evaluationRepository.findAll();
    }

    // ✅ Buscar evaluaciones por correo del evaluador
    public List<Evaluation> obtenerPorCorreoEvaluador(String correo) {
        var evaluador = evaluadorRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Evaluador no encontrado con correo: " + correo));
        return evaluationRepository.findByEvaluador(evaluador);
    }

    // ✅ Eliminar evaluación
    public void eliminarEvaluacion(Long id) {
        if (!evaluationRepository.existsById(id)) {
            throw new RuntimeException("Evaluación no encontrada con ID: " + id);
        }
        evaluationRepository.deleteById(id);
    }
}
