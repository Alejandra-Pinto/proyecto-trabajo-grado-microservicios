package co.unicauca.degreework.access;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.degreework.domain.entities.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}