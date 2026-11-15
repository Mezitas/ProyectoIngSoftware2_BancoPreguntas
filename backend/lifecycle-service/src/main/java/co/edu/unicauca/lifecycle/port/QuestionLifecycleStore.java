package co.edu.unicauca.lifecycle.port;

import co.edu.unicauca.lifecycle.domain.QuestionLifecycle;
import java.util.Optional;

/** Output port for loading and persisting lifecycle aggregates. */
public interface QuestionLifecycleStore {
    Optional<QuestionLifecycle> findByQuestionId(Long questionId);
    QuestionLifecycle save(QuestionLifecycle lifecycle);
}
