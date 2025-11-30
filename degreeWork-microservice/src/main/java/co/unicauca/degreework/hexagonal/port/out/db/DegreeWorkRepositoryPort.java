// hexagonal/port/out/DegreeWorkRepositoryPort.java
package co.unicauca.degreework.hexagonal.port.out.db;

import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork;
import java.util.List;
import java.util.Optional;

public interface DegreeWorkRepositoryPort {
    DegreeWork save(DegreeWork degreeWork);
    Optional<DegreeWork> findById(Long id);
    List<DegreeWork> findAll();
    void deleteById(Long id);
    List<DegreeWork> findByEstado(EnumEstadoDegreeWork estado);
    List<DegreeWork> listByTeacher(String teacherEmail);
    List<DegreeWork> listByStudent(String studentEmail);
}