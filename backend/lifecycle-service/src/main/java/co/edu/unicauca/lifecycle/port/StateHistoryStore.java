package co.edu.unicauca.lifecycle.port;

import co.edu.unicauca.lifecycle.domain.StateHistoryRecord;
import java.util.List;

/** Output port for the immutable audit trail of state changes. */
public interface StateHistoryStore {
    void append(StateHistoryRecord record);
    List<StateHistoryRecord> findByQuestionId(Long questionId);
}
