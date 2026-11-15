package co.edu.unicauca.lifecycle.port;

import co.edu.unicauca.lifecycle.domain.OperationRecord;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import java.util.Optional;

/** Output port for querying asynchronous command outcomes. */
public interface OperationRecordStore {
    boolean existsByOperationId(String operationId);
    void createPending(String operationId, Long questionId, String type);
    OperationRecord complete(String operationId, OperationStatus status, String message);
    Optional<OperationRecord> findByOperationId(String operationId);
}
