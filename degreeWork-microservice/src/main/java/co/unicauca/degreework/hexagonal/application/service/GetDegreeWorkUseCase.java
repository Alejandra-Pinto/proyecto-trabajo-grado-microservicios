package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GetDegreeWorkUseCase {

    private final DegreeWorkRepositoryPort degreeWorkRepositoryPort;

    @Autowired
    public GetDegreeWorkUseCase(DegreeWorkRepositoryPort degreeWorkRepositoryPort) {
        this.degreeWorkRepositoryPort = degreeWorkRepositoryPort;
    }

    public Optional<DegreeWork> findById(Long id) {
        return degreeWorkRepositoryPort.findById(id);
    }

    public List<DegreeWork> findAll() {
        return degreeWorkRepositoryPort.findAll();
    }

    public List<DegreeWork> findByTeacher(String teacherEmail) {
        return degreeWorkRepositoryPort.listByTeacher(teacherEmail);
    }

    public List<DegreeWork> findByStudent(String studentEmail) {
        return degreeWorkRepositoryPort.listByStudent(studentEmail);
    }

    public List<DegreeWork> findByEstado(EnumEstadoDegreeWork estado) {
        return degreeWorkRepositoryPort.findByEstado(estado);
    }
}