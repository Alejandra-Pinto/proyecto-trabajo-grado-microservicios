package co.unicauca.degreework.hexagonal.adapter.out.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.degreework.hexagonal.domain.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}