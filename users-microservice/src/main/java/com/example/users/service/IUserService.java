package com.example.users.service;

import com.example.users.entity.User;
import com.example.users.infra.dto.UserRequest;
import java.util.List;
import java.util.Optional;

public interface IUserService {

    User register(UserRequest request);

    User login(String email, String password);

    List<User> getAllUsers();

    Optional<User> findById(Long id);

    List<User> findByRole(String role);
    
    Optional<User> findByEmail(String email);


}
