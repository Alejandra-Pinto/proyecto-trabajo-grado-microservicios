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
public interface DegreeWorkRepository extends JpaRepository<DegreeWork, Integer> {
    List<DegreeWork> findByEstado(EnumEstadoDegreeWork estado);

    @Query("""
            SELECT d FROM DegreeWork d
            JOIN d.estudiantes e
            WHERE e.email = :correo
            """)
    Optional<DegreeWork> findByCorreoEstudiante(@Param("correo") String correo);

    @Query("SELECT d FROM DegreeWork d JOIN d.formatosA f WHERE f.id = :documentId " +
           "OR f.id IN (SELECT a.id FROM d.anteproyectos a WHERE a.id = :documentId) " +
           "OR f.id IN (SELECT c.id FROM d.cartasAceptacion c WHERE c.id = :documentId)")
    Optional<DegreeWork> findByDocumentId(@Param("documentId") Long documentId);

}
