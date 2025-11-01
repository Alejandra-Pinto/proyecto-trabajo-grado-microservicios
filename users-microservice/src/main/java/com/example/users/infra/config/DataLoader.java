package com.example.users.infra.config;

import com.example.users.entity.Teacher;
import com.example.users.entity.Student;
import com.example.users.repository.ProfessorRepository;
import com.example.users.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;

    public DataLoader(ProfessorRepository professorRepository, StudentRepository studentRepository) {
        this.professorRepository = professorRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        // Solo si la base está vacía
        if (professorRepository.count() == 0 && studentRepository.count() == 0) {

            // Profesores
            Teacher p1 = new Teacher("Dr. Juan Gomez", "juan@unicauca.edu.co");
            Teacher p2 = new Teacher("Dra. Ana Ruiz", "ana@unicauca.edu.co");
            Teacher p3 = new Teacher("Dr. Carlos Perez", "carlos@unicauca.edu.co");

            // Estudiantes
            Student s1 = new Student("Laura Lopez", "laura@unicauca.edu.co", "@Laura123");
            Student s2 = new Student("Marcos Jimenez", "marcos@unicauca.edu.co", "@Marcos123");

            professorRepository.save(p1);
            professorRepository.save(p2);
            professorRepository.save(p3);

            studentRepository.save(s1);
            studentRepository.save(s2);

            System.out.println("Profesores y estudiantes cargados correctamente.");
        }
    }
}

