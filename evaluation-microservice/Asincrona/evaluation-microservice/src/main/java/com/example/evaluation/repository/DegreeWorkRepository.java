package com.example.evaluation.repository;

import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeWorkRepository extends JpaRepository<DegreeWork, Long> {
        List<DegreeWork> findByEstado(EnumEstadoDegreeWork estado);

        @Query("""
                        SELECT d FROM DegreeWork d
                        JOIN d.estudiantesEmails e
                        WHERE e = :correo
                        """)
        Optional<DegreeWork> findByCorreoEstudiante(@Param("correo") String correo);

        @Query(value = """
                        SELECT DISTINCT dw.*
                        FROM degree_works dw
                        LEFT JOIN degree_works_formatosa fa ON fa.degree_work_id = dw.id
                        LEFT JOIN degree_works_anteproyectos ap ON ap.degree_work_id = dw.id
                        LEFT JOIN degree_works_cartas_aceptacion ca ON ca.degree_work_id = dw.id
                        WHERE fa.formatosa_id = :documentId
                        OR ap.anteproyectos_id = :documentId
                        OR ca.cartas_aceptacion_id = :documentId
                        """, nativeQuery = true)
        Optional<DegreeWork> findByDocumentId(@Param("documentId") Long documentId);

}
