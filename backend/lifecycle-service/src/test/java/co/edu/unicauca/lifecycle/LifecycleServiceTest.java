package co.edu.unicauca.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.lifecycle.domain.QuestionLifecycle;
import co.edu.unicauca.lifecycle.domain.StateHistoryRecord;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.port.OperationRecordStore;
import co.edu.unicauca.lifecycle.port.QuestionLifecycleStore;
import co.edu.unicauca.lifecycle.port.StateHistoryStore;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LifecycleServiceTest {

    @Mock
    private QuestionLifecycleStore states;
    @Mock
    private StateHistoryStore history;
    @Mock
    private OperationRecordStore operations;
    @InjectMocks
    private LifecycleService service;

    @Test
    void registerCreatesStateAndHistoryOnlyOnce() {
        when(states.findByQuestionId(10L)).thenReturn(Optional.empty(), Optional.of(QuestionLifecycle.draft(10L, 4L)));
        when(states.save(any(QuestionLifecycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, QuestionLifecycle.class));

        assertEquals(QuestionStatus.DRAFT, service.register(10L, 4L).status());
        assertEquals(QuestionStatus.DRAFT, service.register(10L, 4L).status());

        verify(states).save(any(QuestionLifecycle.class));
        verify(history).append(any(StateHistoryRecord.class));
    }

    @Test
    void transitionPersistsAggregateAndAuditEntry() {
        QuestionLifecycle lifecycle = QuestionLifecycle.draft(10L, 4L);
        when(states.findByQuestionId(10L)).thenReturn(Optional.of(lifecycle));
        when(states.save(any(QuestionLifecycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, QuestionLifecycle.class));

        var result = service.transition(10L, QuestionStatus.PENDING_REVIEW, 4L, null, null);

        assertEquals(QuestionStatus.DRAFT, result.previousStatus());
        assertEquals(QuestionStatus.PENDING_REVIEW, result.status());
        verify(history).append(any(StateHistoryRecord.class));
    }
}
