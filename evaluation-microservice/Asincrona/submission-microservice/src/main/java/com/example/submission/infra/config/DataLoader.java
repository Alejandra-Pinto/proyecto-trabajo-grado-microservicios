package com.example.submission.infra.config;

import com.example.submission.entity.Professor;
import com.example.submission.entity.Student;
import com.example.submission.repository.ProfessorRepository;
import com.example.submission.repository.StudentRepository;
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
        // 🔹 Solo si la base está vacía
        if (professorRepository.count() == 0 && studentRepository.count() == 0) {

            // Profesores
            Professor p1 = new Professor("Dr. Juan Gomez", "juan@unicauca.edu.co");
            Professor p2 = new Professor("Dra. Ana Ruiz", "ana@unicauca.edu.co");
            Professor p3 = new Professor("Dr. Carlos Perez", "carlos@unicauca.edu.co");

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

