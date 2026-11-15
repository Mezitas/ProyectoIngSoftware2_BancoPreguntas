package co.edu.unicauca.lifecycle.adapter.persistence;

import co.edu.unicauca.lifecycle.domain.OperationRecord;
import co.edu.unicauca.lifecycle.model.OperationResultEntity;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.port.OperationRecordStore;
import co.edu.unicauca.lifecycle.repository.OperationResultRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Maps persisted command results to the application-facing read model. */
@Repository
public class OperationRecordPersistenceAdapter implements OperationRecordStore {

    private final OperationResultRepository repository;

    public OperationRecordPersistenceAdapter(OperationResultRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByOperationId(String operationId) {
        return repository.existsById(operationId);
    }

    @Override
    public void createPending(String operationId, Long questionId, String type) {
        repository.save(new OperationResultEntity(operationId, questionId, type));
    }

    @Override
    public OperationRecord complete(String operationId, OperationStatus status, String message) {
        OperationResultEntity entity = repository.findById(operationId)
                .orElseThrow(() -> new IllegalStateException("No existe la operación " + operationId));
        entity.complete(status, message);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<OperationRecord> findByOperationId(String operationId) {
        return repository.findById(operationId).map(OperationRecordPersistenceAdapter::toDomain);
    }

    private static OperationRecord toDomain(OperationResultEntity entity) {
        return new OperationRecord(entity.getOperationId(),
                entity.getQuestionId(), entity.getType(), entity.getStatus(), entity.getMessage(),
                entity.getCreatedAt(), entity.getCompletedAt());
    }
}
