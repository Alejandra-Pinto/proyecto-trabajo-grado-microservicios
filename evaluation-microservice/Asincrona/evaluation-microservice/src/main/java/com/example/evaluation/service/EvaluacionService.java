package com.example.evaluation.service;

import com.example.evaluation.entity.Document;
import com.example.evaluation.entity.Evaluation;
import com.example.evaluation.infra.messaging.EvaluationPublisher;
import com.example.evaluation.entity.Evaluador;
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

    // ✅ Crear evaluación (ahora usando correo del evaluador)
    public Evaluation crearEvaluacion(Long documentId, String evaluadorCorreo, String resultado, String tipo) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("❌ Documento no encontrado con ID: " + documentId));

        Evaluador evaluador = evaluadorRepository.findByCorreo(evaluadorCorreo)
                .orElseThrow(() -> new RuntimeException("❌ Evaluador no encontrado con correo: " + evaluadorCorreo));

        Evaluation evaluacion = new Evaluation();
        evaluacion.setDocument(document);
        evaluacion.setEvaluador(evaluador);
        evaluacion.setResultado(resultado);
        evaluacion.setType(tipo);
        evaluacion.setSentAt(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(evaluacion);

        // 🟢 Enviar evento al exchange
        com.example.evaluation.infra.dto.EvaluationRequestDTO dto = new com.example.evaluation.infra.dto.EvaluationRequestDTO();
        dto.setDocumentId(documentId);
        dto.setEvaluadorCorreo(evaluadorCorreo);
        dto.setResultado(resultado);
        dto.setTipo(tipo);

        evaluationPublisher.publicarEvaluacion(dto);

        return saved;
    }

    // ✅ Listar todas las evaluaciones
    public List<Evaluation> listarEvaluaciones() {
        return evaluationRepository.findAll();
    }

    // ✅ Buscar todas las evaluaciones realizadas por un evaluador (por correo)
    public List<Evaluation> obtenerPorCorreoEvaluador(String correoEvaluador) {
        return evaluationRepository.findByEvaluadorCorreo(correoEvaluador);
    }

    // ✅ Buscar evaluación por ID (opcional, aún útil para admin o pruebas)
    public Evaluation obtenerPorId(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con ID: " + id));
    }

    // ✅ Eliminar evaluación por ID
    public void eliminarEvaluacion(Long id) {
        if (!evaluationRepository.existsById(id)) {
            throw new RuntimeException("La evaluación no existe");
        }
        evaluationRepository.deleteById(id);
    }
}
