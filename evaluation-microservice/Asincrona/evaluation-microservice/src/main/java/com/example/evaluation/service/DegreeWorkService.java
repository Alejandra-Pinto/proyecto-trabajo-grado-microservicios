
package com.example.evaluation.service;

import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.Evaluador;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.EvaluadorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DegreeWorkService {

    @Autowired
    private DegreeWorkRepository degreeWorkRepository;

    @Autowired
    private EvaluadorRepository evaluadorRepository;

    // Método para listar los anteproyectos
    public List<DegreeWork> listarAnteproyectos() {
        return degreeWorkRepository.findByEstado(EnumEstadoDegreeWork.ANTEPROYECTO);
    }

    // Método para asignar evaluadores
    public DegreeWork asignarEvaluadores(Integer degreeWorkId, Long evaluador1Id, Long evaluador2Id) {
        // Buscar el trabajo de grado
        DegreeWork degreeWork = degreeWorkRepository.findById(degreeWorkId)
                .orElseThrow(() -> new RuntimeException("Trabajo de grado no encontrado"));

        // Buscar los evaluadores
        Evaluador evaluador1 = evaluadorRepository.findById(evaluador1Id)
                .orElseThrow(() -> new RuntimeException("Evaluador 1 no encontrado"));
        Evaluador evaluador2 = evaluadorRepository.findById(evaluador2Id)
                .orElseThrow(() -> new RuntimeException("Evaluador 2 no encontrado"));

        // Asignar los evaluadores al trabajo
        degreeWork.setEvaluador1(evaluador1);
        degreeWork.setEvaluador2(evaluador2);

        // No tocamos el estado, se mantiene igual (por ejemplo ANTEPROYECTO)
        return degreeWorkRepository.save(degreeWork);
    }

    public List<DegreeWork> listarTodos() {
        return degreeWorkRepository.findAll();
    }

    public DegreeWork obtenerPorId(Integer id) {
        return degreeWorkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajo de grado no encontrado"));
    }

}
