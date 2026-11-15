package co.edu.unicauca.lifecycle.adapter.persistence;

import co.edu.unicauca.lifecycle.domain.QuestionLifecycle;
import co.edu.unicauca.lifecycle.model.QuestionStateEntity;
import co.edu.unicauca.lifecycle.port.QuestionLifecycleStore;
import co.edu.unicauca.lifecycle.repository.QuestionStateRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Maps lifecycle aggregates to the JPA representation without exposing JPA to the domain. */
@Repository
public class QuestionLifecyclePersistenceAdapter implements QuestionLifecycleStore {

    private final QuestionStateRepository repository;

    public QuestionLifecyclePersistenceAdapter(QuestionStateRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<QuestionLifecycle> findByQuestionId(Long questionId) {
        return repository.findById(questionId).map(QuestionLifecyclePersistenceAdapter::toDomain);
    }

    @Override
    public QuestionLifecycle save(QuestionLifecycle lifecycle) {
        QuestionStateEntity entity = repository.findById(lifecycle.questionId())
                .map(existing -> {
                    existing.apply(lifecycle.status(), lifecycle.changedBy(), lifecycle.comment(),
                            lifecycle.changedAt());
                    return existing;
                })
                .orElseGet(() -> new QuestionStateEntity(lifecycle.questionId(), lifecycle.status(),
                        lifecycle.createdBy(), lifecycle.changedBy(), lifecycle.comment(), lifecycle.changedAt()));
        return toDomain(repository.save(entity));
    }

    private static QuestionLifecycle toDomain(QuestionStateEntity entity) {
        return QuestionLifecycle.reconstitute(entity.getQuestionId(), entity.getStatus(), entity.getCreatedBy(),
                entity.getChangedBy(), entity.getComment(), entity.getChangedAt());
    }
}
