package co.edu.unicauca.lifecycle.service;

import co.edu.unicauca.lifecycle.dto.*;
import co.edu.unicauca.lifecycle.exception.BadRequestException;
import co.edu.unicauca.lifecycle.exception.NotFoundException;
import co.edu.unicauca.lifecycle.model.*;
import co.edu.unicauca.lifecycle.repository.OperationResultRepository;
import co.edu.unicauca.lifecycle.repository.QuestionStateRepository;
import co.edu.unicauca.lifecycle.repository.StateHistoryRepository;
import co.edu.unicauca.lifecycle.state.LifecycleContext;
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

    private final QuestionStateRepository states;
    private final StateHistoryRepository history;
    private final OperationResultRepository operations;

    public LifecycleService(QuestionStateRepository states, StateHistoryRepository history,
                            OperationResultRepository operations) {
        this.states = states;
        this.history = history;
        this.operations = operations;
    }

    /** Registra una pregunta nueva en DRAFT. Idempotente: si ya existe devuelve su estado actual. */
    @Transactional
    public StateDTO register(Long questionId, Long createdBy) {
        QuestionStateEntity entity = states.findById(questionId).orElse(null);
        if (entity == null) {
            entity = states.save(new QuestionStateEntity(questionId, QuestionStatus.DRAFT, createdBy));
            history.save(new StateHistoryEntity(questionId, null, QuestionStatus.DRAFT, createdBy,
                    "Pregunta creada", null));
        }
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public StateDTO get(Long questionId) {
        return toDto(find(questionId));
    }

    @Transactional(readOnly = true)
    public List<HistoryEntryDTO> history(Long questionId) {
        find(questionId);
        return history.findByQuestionIdOrderByIdAsc(questionId).stream()
                .map(h -> new HistoryEntryDTO(h.getFromStatus(), h.getToStatus(), h.getChangedBy(),
                        h.getComment(), h.getOperationId(), h.getChangedAt()))
                .toList();
    }

    /**
     * Aplica una transición validada por el patrón State. Si no es válida lanza
     * InvalidTransitionException y la transacción no escribe nada.
     */
    @Transactional
    public TransitionResultDTO transition(Long questionId, QuestionStatus target, Long requestedBy,
                                          String comment, String operationId) {
        QuestionStateEntity entity = find(questionId);
        if (target == QuestionStatus.REJECTED && (comment == null || comment.isBlank())) {
            throw new BadRequestException("Para rechazar una pregunta debe indicar el motivo (comment)");
        }
        QuestionStatus previous = entity.getStatus();

        LifecycleContext ctx = LifecycleContext.of(previous);
        ctx.transitionTo(target);   // lanza InvalidTransitionException si no es válida

        entity.apply(ctx.getStatus(), requestedBy, comment);
        states.save(entity);
        history.save(new StateHistoryEntity(questionId, previous, ctx.getStatus(), requestedBy, comment, operationId));
        return new TransitionResultDTO(questionId, previous, ctx.getStatus(), comment, entity.getChangedAt());
    }

    @Transactional(readOnly = true)
    public OperationDTO getOperation(String operationId) {
        return operations.findById(operationId)
                .map(o -> new OperationDTO(o.getOperationId(), o.getQuestionId(), o.getType(), o.getStatus(),
                        o.getMessage(), o.getCreatedAt(), o.getCompletedAt()))
                .orElseThrow(() -> new NotFoundException("No existe la operación " + operationId));
    }

    private QuestionStateEntity find(Long questionId) {
        return states.findById(questionId)
                .orElseThrow(() -> new NotFoundException("La pregunta " + questionId + " no está registrada en el ciclo de vida"));
    }

    private StateDTO toDto(QuestionStateEntity e) {
        return new StateDTO(e.getQuestionId(), e.getStatus(), e.getCreatedBy(), e.getChangedBy(),
                e.getComment(), e.getChangedAt());
    }
}
