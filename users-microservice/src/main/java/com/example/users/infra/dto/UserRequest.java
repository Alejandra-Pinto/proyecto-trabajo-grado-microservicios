package com.example.users.infra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para recibir información de registro o actualización de usuarios.
 * Se usa en los controladores para validar los datos entrantes desde Postman o el frontend.
 */
public class UserRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacío")
    private String lastName;

    private String phone; // opcional

    @NotBlank(message = "El programa no puede estar vacío")
    private String program;

    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "Debe ser un correo válido con formato institucional")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*]).+$",
        message = "La contraseña debe contener una mayúscula, un número y un carácter especial"
    )
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    private String role; // STUDENT, PROFESSOR, COORDINATOR, ADMIN...

    private String status; // Por defecto puede ser 'ACEPTADO' o 'PENDIENTE'

    // Getters y Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
