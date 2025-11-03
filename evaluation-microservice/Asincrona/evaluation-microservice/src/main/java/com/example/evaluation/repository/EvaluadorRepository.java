package com.example.evaluation.repository;

import com.example.evaluation.entity.Evaluador;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluadorRepository extends JpaRepository<Evaluador, Long> {
    Optional<Evaluador> findByCorreo(String correo);
}
