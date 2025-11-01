package com.example.users.repository;

import com.example.users.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Teacher, Long> { }