package com.example.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.notification.repository.UserRepository;
import com.example.notification.infra.config.dto.NotificationEventDTO;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipientService {

    @Autowired
    private UserRepository userRepository;

    public List<String> resolveRecipients(NotificationEventDTO dto) {
        List<String> emails = new ArrayList<>();

        // 1. Si hay emails específicos en recipientEmails, usarlos
        if (dto.getRecipientEmails() != null && !dto.getRecipientEmails().isEmpty()) {
            emails.addAll(dto.getRecipientEmails());
        }

        // 2. Si hay un rol objetivo, buscar todos los usuarios con ese rol
        if (dto.getTargetRole() != null && !dto.getTargetRole().isEmpty()) {
            List<String> roleEmails = userRepository.findEmailsByRole(dto.getTargetRole());
            emails.addAll(roleEmails);
        }

        // 3. Para eventos específicos, agregar emails adicionales
        switch (dto.getEventType()) {
            case "FORMATO_A_SUBIDO":
                // Ya se maneja con targetRole = "COORDINATOR"
                break;
                
            case "FORMATO_A_EVALUADO":
                // Notificar al director y codirectores
                addIfNotNull(emails, dto.getDirectorEmail());
                addIfNotNull(emails, dto.getCoDirector1Email());
                addIfNotNull(emails, dto.getCoDirector2Email());
                break;
                
            case "ANTEPROYECTO_SUBIDO":
                // Ya se maneja con targetRole = "DEPARTMENT_HEAD" o similar
                break;
                
            case "EVALUADORES_ASIGNADOS":
                // Notificar a los evaluadores asignados
                if (dto.getEvaluatorEmails() != null) {
                    emails.addAll(dto.getEvaluatorEmails());
                }
                break;
        }

        return emails.stream().distinct().toList(); // Eliminar duplicados
    }

    private void addIfNotNull(List<String> list, String email) {
        if (email != null && !email.trim().isEmpty()) {
            list.add(email.trim().toLowerCase());
        }
    }
}