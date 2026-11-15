package co.edu.unicauca.lifecycle.adapter.persistence;

import co.edu.unicauca.lifecycle.domain.StateHistoryRecord;
import co.edu.unicauca.lifecycle.model.StateHistoryEntity;
import co.edu.unicauca.lifecycle.port.StateHistoryStore;
import co.edu.unicauca.lifecycle.repository.StateHistoryRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Persists and reconstructs audit records through Spring Data JPA. */
@Repository
public class StateHistoryPersistenceAdapter implements StateHistoryStore {

    private final StateHistoryRepository repository;

    public StateHistoryPersistenceAdapter(StateHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void append(StateHistoryRecord record) {
        repository.save(new StateHistoryEntity(record.questionId(), record.fromStatus(), record.toStatus(),
                record.changedBy(), record.comment(), record.operationId()));
    }

    @Override
    public List<StateHistoryRecord> findByQuestionId(Long questionId) {
        return repository.findByQuestionIdOrderByIdAsc(questionId).stream()
                .map(entity -> new StateHistoryRecord(entity.getQuestionId(), entity.getFromStatus(),
                        entity.getToStatus(), entity.getChangedBy(), entity.getComment(), entity.getOperationId(),
                        entity.getChangedAt()))
                .toList();
    }
}
