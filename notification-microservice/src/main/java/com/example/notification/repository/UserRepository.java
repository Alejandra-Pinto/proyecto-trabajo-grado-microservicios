package com.example.notification.repository;

import com.example.notification.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u.email FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    List<String> findEmailsByRole(@Param("role") String role);
    
    List<User> findByRole(String role);
    
    User findByEmail(String email);
}