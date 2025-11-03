package com.example.notification.controller;

import com.example.notification.entity.Notification;
import com.example.notification.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository repo;

    public NotificationController(NotificationRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Notification> getAll() {
        return repo.findAll();
    }
}
