package com.example.users.service;

import com.example.users.entity.Admin;
//import com.example.users.entity.User;
import com.example.users.repository.AdminRepository;
import com.example.users.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AdminService implements IAdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Roles que requieren aprobación manual
    private static final Set<String> APPROVABLE_ROLES = Set.of("COORDINATOR", "DEPARTMENT_HEAD");

    public AdminService(AdminRepository adminRepository, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Admin register(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public String approveUser(String email) {
        var optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty())
            return "Usuario con email " + email + " no encontrado.";

        var user = optUser.get();
        if (!APPROVABLE_ROLES.contains(user.getRole().toUpperCase()))
            return "Solo se pueden aprobar roles: " + APPROVABLE_ROLES;

        user.setStatus("ACEPTADO");
        userRepository.save(user);
        return "Usuario " + user.getEmail() + " (" + user.getRole() + ") aprobado.";
    }

    public String rejectUser(String email) {
        var optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty())
            return "Usuario con email " + email + " no encontrado.";

        var user = optUser.get();
        if (!APPROVABLE_ROLES.contains(user.getRole().toUpperCase()))
            return "Solo se pueden rechazar roles: " + APPROVABLE_ROLES;

        user.setStatus("RECHAZADO");
        userRepository.save(user);
        return "Usuario " + user.getEmail() + " (" + user.getRole() + ") rechazado.";
    }

    public Optional<Admin> login(String email, String rawPassword) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (passwordEncoder.matches(rawPassword, admin.getPassword())) {
                return Optional.of(admin);
            }
        }
        return Optional.empty();
    }
}
