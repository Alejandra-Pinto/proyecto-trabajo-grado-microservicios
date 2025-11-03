package com.example.users.infra.config;

import com.example.users.entity.*;
import com.example.users.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataLoader(UserRepository userRepository,
                      AdminRepository adminRepository,
                      ProfessorRepository professorRepository,
                      StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.professorRepository = professorRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        // Solo si la base está vacía
        if (userRepository.count() == 0) {

            // ============ ADMIN ============
            Admin admin = new Admin();
            admin.setFirstName("Administrador");
            admin.setLastName("Sistema");
            admin.setEmail("admin@unicauca.edu.co");
            admin.setPhone("0000000000");
            admin.setPassword(passwordEncoder.encode("@Admin123")); // cumple con regex
            adminRepository.save(admin);

            // ============ PROFESORES ============
            Teacher p1 = new Teacher("Dr. Juan Gomez", "juan@unicauca.edu.co");
            p1.setPassword(passwordEncoder.encode("@Juan123"));
            p1.setRole("PROFESSOR");
            p1.setStatus("ACEPTADO");
            professorRepository.save(p1);

            Teacher p2 = new Teacher("Dra. Ana Ruiz", "ana@unicauca.edu.co");
            p2.setPassword(passwordEncoder.encode("@Ana123"));
            p2.setRole("PROFESSOR");
            p2.setStatus("ACEPTADO");
            professorRepository.save(p2);

            Teacher p3 = new Teacher("Dr. Carlos Perez", "carlos@unicauca.edu.co");
            p3.setPassword(passwordEncoder.encode("@Carlos123"));
            p3.setRole("PROFESSOR");
            p3.setStatus("ACEPTADO");
            professorRepository.save(p3);

            // ============ ESTUDIANTES ============
            Student s1 = new Student("Laura Lopez", "laura@unicauca.edu.co", passwordEncoder.encode("@Laura123"));
            s1.setRole("STUDENT");
            s1.setStatus("ACEPTADO");
            studentRepository.save(s1);

            Student s2 = new Student("Marcos Jimenez", "marcos@unicauca.edu.co", passwordEncoder.encode("@Marcos123"));
            s2.setRole("STUDENT");
            s2.setStatus("ACEPTADO");
            studentRepository.save(s2);

            System.out.println("Usuarios base (admin, profesores y estudiantes) cargados correctamente con contraseñas cifradas.");
        }
    }
}
