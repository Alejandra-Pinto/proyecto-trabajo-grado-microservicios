package com.example.evaluation.repository;

import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeWorkRepository extends JpaRepository<DegreeWork, Integer> {
    List<DegreeWork> findByEstado(EnumEstadoDegreeWork estado);

    Optional<DegreeWork> findByCorreoEstudiante(String correo);
}
