package com.example.users.service;

import com.example.users.entity.Admin;
import com.example.users.entity.User;
import com.example.users.infra.dto.EvaluatorAssignmentDTO;

import java.util.List;
import java.util.Optional;

public interface IAdminService {

    Admin register(Admin admin);
    String approveUser(String email);
    String rejectUser(String email);
    Optional<Admin> login(String email, String rawPassword);

    // Nuevos métodos agregados
    String assignEvaluator(EvaluatorAssignmentDTO dto);
    List<User> listPotentialEvaluators();
}
