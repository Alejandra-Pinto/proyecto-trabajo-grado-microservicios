package co.unicauca.degreework.access;

import co.unicauca.degreework.domain.entities.DegreeWork;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DegreeWorkRepository extends JpaRepository<DegreeWork, Long> {
    @Query("""
           SELECT d FROM DegreeWork  d
           WHERE d.directorProyecto.email = :teacherEmail
           """)
    List<DegreeWork> listByTeacher(String teacherEmail);

    @Query("SELECT d FROM DegreeWork d JOIN d.estudiantes e WHERE e.email = :email")
    List<DegreeWork> listByStudent(@Param("email") String email);
}
