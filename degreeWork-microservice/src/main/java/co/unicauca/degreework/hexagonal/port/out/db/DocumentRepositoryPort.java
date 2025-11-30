// hexagonal/port/out/DocumentRepositoryPort.java
package co.unicauca.degreework.hexagonal.port.out.db;

import co.unicauca.degreework.hexagonal.domain.model.Document;

public interface DocumentRepositoryPort {
    Document save(Document document);
}