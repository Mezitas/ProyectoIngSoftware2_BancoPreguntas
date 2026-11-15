package co.edu.unicauca.lifecycle.service;

import co.edu.unicauca.lifecycle.dto.*;
import co.edu.unicauca.lifecycle.exception.NotFoundException;
import co.edu.unicauca.lifecycle.domain.QuestionLifecycle;
import co.edu.unicauca.lifecycle.domain.StateHistoryRecord;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.port.OperationRecordStore;
import co.edu.unicauca.lifecycle.port.QuestionLifecycleStore;
import co.edu.unicauca.lifecycle.port.StateHistoryStore;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autoridad única sobre el estado de las preguntas. Todas las escrituras de estado
 * pasan por {@link #transition}, ya sea desde REST (Review Service) o desde un
 * comando de RabbitMQ (Question Service).
 */
@Service
public class LifecycleService {

    private final QuestionLifecycleStore states;
    private final StateHistoryStore history;
    private final OperationRecordStore operations;

    public LifecycleService(QuestionLifecycleStore states, StateHistoryStore history,
                            OperationRecordStore operations) {
        this.states = states;
        this.history = history;
        this.operations = operations;
    }

    /** Registra una pregunta nueva en DRAFT. Idempotente: si ya existe devuelve su estado actual. */
    @Transactional
    public StateDTO register(Long questionId, Long createdBy) {
        QuestionLifecycle lifecycle = states.findByQuestionId(questionId).orElse(null);
        if (lifecycle == null) {
            lifecycle = QuestionLifecycle.draft(questionId, createdBy);
            lifecycle = states.save(lifecycle);
            history.append(new StateHistoryRecord(questionId, null, QuestionStatus.DRAFT, createdBy,
                    "Pregunta creada", null, Instant.now()));
        }
        return toDto(lifecycle);
    }

    @Transactional(readOnly = true)
    public StateDTO get(Long questionId) {
        return toDto(find(questionId));
    }

    @Transactional(readOnly = true)
    public List<HistoryEntryDTO> history(Long questionId) {
        find(questionId);
        return history.findByQuestionId(questionId).stream()
                .map(entry -> new HistoryEntryDTO(entry.fromStatus(), entry.toStatus(), entry.changedBy(),
                        entry.comment(), entry.operationId(), entry.changedAt()))
                .toList();
    }

    /**
     * Aplica una transición validada por el patrón State. Si no es válida lanza
     * InvalidTransitionException y la transacción no escribe nada.
     */
    @Transactional
    public TransitionResultDTO transition(Long questionId, QuestionStatus target, Long requestedBy,
                                          String comment, String operationId) {
        QuestionLifecycle lifecycle = find(questionId);
        QuestionStatus previous = lifecycle.status();
        lifecycle.transitionTo(target, requestedBy, comment);
        QuestionLifecycle persisted = states.save(lifecycle);
        history.append(new StateHistoryRecord(questionId, previous, persisted.status(), requestedBy,
                comment, operationId, persisted.changedAt()));
        return new TransitionResultDTO(questionId, previous, persisted.status(), comment, persisted.changedAt());
    }

    @Transactional(readOnly = true)
    public OperationDTO getOperation(String operationId) {
        return operations.findByOperationId(operationId)
                .map(operation -> new OperationDTO(operation.operationId(), operation.questionId(), operation.type(),
                        operation.status(), operation.message(), operation.createdAt(), operation.completedAt()))
                .orElseThrow(() -> new NotFoundException("No existe la operación " + operationId));
    }

    private QuestionLifecycle find(Long questionId) {
        return states.findByQuestionId(questionId)
                .orElseThrow(() -> new NotFoundException("La pregunta " + questionId + " no está registrada en el ciclo de vida"));
    }

    private StateDTO toDto(QuestionLifecycle lifecycle) {
        return new StateDTO(lifecycle.questionId(), lifecycle.status(), lifecycle.createdBy(),
                lifecycle.changedBy(), lifecycle.comment(), lifecycle.changedAt());
    }
}
