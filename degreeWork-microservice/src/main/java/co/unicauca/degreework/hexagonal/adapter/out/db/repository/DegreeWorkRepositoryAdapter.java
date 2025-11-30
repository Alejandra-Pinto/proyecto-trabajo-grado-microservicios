package co.unicauca.degreework.hexagonal.adapter.out.db.repository;

import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import co.unicauca.degreework.hexagonal.adapter.out.repository.DegreeWorkRepository; // ← Tu repositorio actual
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DegreeWorkRepositoryAdapter implements DegreeWorkRepositoryPort {

    private final DegreeWorkRepository degreeWorkRepository; // ← Usa tu repositorio actual

    public DegreeWorkRepositoryAdapter(DegreeWorkRepository degreeWorkRepository) {
        this.degreeWorkRepository = degreeWorkRepository;
    }

    @Override
    public DegreeWork save(DegreeWork degreeWork) {
        return degreeWorkRepository.save(degreeWork);
    }

    @Override
    public Optional<DegreeWork> findById(Long id) {
        return degreeWorkRepository.findById(id);
    }

    @Override
    public List<DegreeWork> findAll() {
        return degreeWorkRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        degreeWorkRepository.deleteById(id);
    }

    @Override
    public List<DegreeWork> findByEstado(EnumEstadoDegreeWork estado) {
        return degreeWorkRepository.findByEstado(estado);
    }

    @Override
    public List<DegreeWork> listByTeacher(String teacherEmail) {
        return degreeWorkRepository.listByTeacher(teacherEmail);
    }

    @Override
    public List<DegreeWork> listByStudent(String studentEmail) {
        return degreeWorkRepository.listByStudent(studentEmail);
    }
}