package com.example.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.submission.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}