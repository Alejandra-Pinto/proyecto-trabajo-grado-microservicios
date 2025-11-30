// hexagonal/adapter/out/db/repository/DocumentRepositoryAdapter.java
package co.unicauca.degreework.hexagonal.adapter.out.db.repository;

import co.unicauca.degreework.hexagonal.domain.model.Document;
import co.unicauca.degreework.hexagonal.port.out.db.DocumentRepositoryPort;
import co.unicauca.degreework.hexagonal.adapter.out.repository.DocumentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepositoryAdapter implements DocumentRepositoryPort {

    private final DocumentRepository documentRepository;

    public DocumentRepositoryAdapter(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document save(Document document) {
        return documentRepository.save(document);
    }
}