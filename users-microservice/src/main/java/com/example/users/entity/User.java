package com.example.users.entity;

import java.time.Instant;
import java.util.regex.Pattern;

import com.example.users.entity.enums.EnumDepartment;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // para herencia
@DiscriminatorColumn(name = "role_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phone; // opcional
    private String program;
    @Enumerated(EnumType.STRING)
    private EnumDepartment department;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // cifrada

    private String role;  // "STUDENT", "PROFESSOR", "COORDINATOR", etc.
    private String status; // "ACEPTADO", "INACTIVO", etc.

    private Instant createdAt;
    private Instant updatedAt;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Constructor con validaciones
    public User(String firstName, String lastName, String phone, String program,
                String email, String password, String role) {
        setFirstName(firstName);
        setLastName(lastName);
        setPhone(phone);
        setProgram(program);
        setEmail(email);
        setPassword(password);
        setRole(role);
        //this.status = "ACEPTADO";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public User() {}

    // --- Getters y Setters con validaciones ---
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío.");
        }
        this.lastName = lastName.trim();
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        if (phone != null && !phone.matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos numéricos.");
        }
        this.phone = phone;
    }

    public String getProgram() {
        return program;
    }
    public void setProgram(String program) {
        if (program == null || program.trim().isEmpty()) {
            throw new IllegalArgumentException("El programa no puede estar vacío.");
        }
        this.program = program.trim();
        assignDepartmentBasedOnProgram(program);
    }


    private void assignDepartmentBasedOnProgram(String program) {
        if (program == null) {
            this.department = null;
            return;
        }

        String normalized = program.trim().toLowerCase();

        if (normalized.contains("sistema")) {
            this.department = EnumDepartment.SISTEMAS;
        } else if (normalized.contains("electr")) {
            this.department = EnumDepartment.ELECTRONICA;
        } else if (normalized.contains("civil")) {
            this.department = EnumDepartment.CIVIL;
        } else {
            this.department = null; // O podrías definir un EnumDepartment.GENERAL si quisieras
        }
    }


    public EnumDepartment getDepartment() {
        return department;
    }

    public void setDepartment(EnumDepartment department) {
        if (department == null) {
            throw new IllegalArgumentException("El departamento no puede ser nulo.");
        }
        this.department = department;
    }


    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Contraseña insegura. Debe tener al menos 6 caracteres.");
        }
        this.password = password; // Aquí se debe cifrar antes de guardar
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Rol inválido. Debe ser STUDENT, PROFESSOR o COORDINATOR.");
        }
        this.role = role;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado no puede estar vacío.");
        }
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- Método abstracto para personalizar comportamiento por rol ---
    public abstract void showDashboard();

    // --- Callback de ciclo de vida JPA ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
