package com.example.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evaluation.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}