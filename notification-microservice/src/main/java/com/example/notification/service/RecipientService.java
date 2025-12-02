package com.example.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.notification.repository.UserRepository;
import com.example.notification.entity.User;
import com.example.notification.infra.config.dto.NotificationEventDTO;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipientService {

    @Autowired
    private UserRepository userRepository;

    public List<String> resolveRecipients(NotificationEventDTO dto) {
        List<String> emails = new ArrayList<>();

        System.out.println("=== RESOLVIENDO DESTINATARIOS ===");
        System.out.println("Evento: " + dto.getEventType());
        System.out.println("Target Role: " + dto.getTargetRole());
        
        // 1. Si hay emails específicos en recipientEmails, usarlos
        if (dto.getRecipientEmails() != null && !dto.getRecipientEmails().isEmpty()) {
            System.out.println("Emails específicos: " + dto.getRecipientEmails());
            emails.addAll(dto.getRecipientEmails());
        }

        // 2. Si hay un rol objetivo, buscar todos los usuarios con ese rol
        if (dto.getTargetRole() != null && !dto.getTargetRole().isEmpty()) {
            System.out.println("Buscando usuarios con rol: " + dto.getTargetRole());
            List<String> roleEmails = userRepository.findEmailsByRole(dto.getTargetRole());
            System.out.println("Emails encontrados por rol: " + roleEmails);
            emails.addAll(roleEmails);
            
            // DEPURACIÓN: Ver todos los usuarios con ese rol
            List<User> usersWithRole = userRepository.findByRole(dto.getTargetRole());
            System.out.println("Usuarios encontrados: " + usersWithRole.size());
            for (User user : usersWithRole) {
                System.out.println("  - " + user.getEmail() + " | " + user.getRole());
            }
        }

        System.out.println("Total destinatarios: " + emails.size());
        System.out.println("Destinatarios: " + emails);
        
        return emails.stream().distinct().toList();
    }
    
    private void addIfNotNull(List<String> list, String email) {
        if (email != null && !email.trim().isEmpty()) {
            list.add(email.trim().toLowerCase());
        }
    }
}