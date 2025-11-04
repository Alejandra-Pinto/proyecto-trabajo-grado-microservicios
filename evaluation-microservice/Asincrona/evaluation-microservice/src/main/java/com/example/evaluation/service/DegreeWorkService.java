package com.example.evaluation.service;

import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.Evaluador;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.infra.dto.DegreeWorkAssignmentDTO;
import com.example.evaluation.infra.messaging.DegreeWorkPublisher;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.EvaluadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DegreeWorkService {

    @Autowired
    private DegreeWorkRepository degreeWorkRepository;

    @Autowired
    private EvaluadorRepository evaluadorRepository;

    @Autowired
    private DegreeWorkPublisher degreeWorkPublisher;

    /**
     * ✅ Listar trabajos en estado ANTEPROYECTO
     */
    public List<DegreeWork> listarAnteproyectos() {
        return degreeWorkRepository.findByEstado(EnumEstadoDegreeWork.ANTEPROYECTO);
    }

    /**
     * ✅ Listar todos los trabajos de grado
     */
    public List<DegreeWork> listarTodos() {
        return degreeWorkRepository.findAll();
    }

    /**
     * ✅ Obtener trabajo de grado por correo de un estudiante
     */
    public DegreeWork obtenerPorCorreo(String correo) {
        return degreeWorkRepository.findByCorreoEstudiante(correo)
                .orElseThrow(() -> new RuntimeException(
                        "❌ No se encontró trabajo de grado del estudiante con correo: " + correo));
    }

    /**
     * ✅ Asignar evaluadores por correo y notificar al microservicio de proyectos
     * 
     * Nota: No se guardan los evaluadores dentro de DegreeWork (ya no existe esa relación),
     * solo se envía la asignación como evento a RabbitMQ.
     */
    @Transactional
    public void asignarEvaluadoresPorCorreo(int degreeWorkId, String correoEvaluador1, String correoEvaluador2) {
        // 1️⃣ Buscar el trabajo de grado
        DegreeWork degreeWork = degreeWorkRepository.findById(degreeWorkId)
                .orElseThrow(() -> new RuntimeException("❌ Trabajo de grado no encontrado con ID: " + degreeWorkId));

        // 2️⃣ Buscar evaluadores por correo
        Evaluador evaluador1 = evaluadorRepository.findByCorreo(correoEvaluador1)
                .orElseThrow(() -> new RuntimeException("❌ Evaluador 1 no encontrado con correo: " + correoEvaluador1));

        Evaluador evaluador2 = evaluadorRepository.findByCorreo(correoEvaluador2)
                .orElseThrow(() -> new RuntimeException("❌ Evaluador 2 no encontrado con correo: " + correoEvaluador2));

        // 3️⃣ Publicar evento de asignación en RabbitMQ
        DegreeWorkAssignmentDTO dto = new DegreeWorkAssignmentDTO(
                degreeWork.getId().intValue(),
                degreeWork.getTitulo(),
                evaluador1.getCorreo(),
                evaluador1.getNombre(),
                evaluador2.getCorreo(),
                evaluador2.getNombre(),
                degreeWork.getEstado().name()
        );

        degreeWorkPublisher.publicarAsignacionEvaluadores(dto);

        System.out.println("✅ Evaluadores asignados y evento publicado a DEGREEWORK SERVICE");
    }
}
