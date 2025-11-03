package com.example.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}