package co.edu.unicauca.lifecycle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.lifecycle.domain.OperationRecord;
import co.edu.unicauca.lifecycle.messaging.OperationResultMessage;
import co.edu.unicauca.lifecycle.messaging.ResultPublisher;
import co.edu.unicauca.lifecycle.messaging.SubmitQuestionForReviewCommand;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.port.OperationRecordStore;
import co.edu.unicauca.lifecycle.service.CommandProcessor;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandProcessorTest {

    @Mock
    private LifecycleService lifecycle;
    @Mock
    private OperationRecordStore operations;
    @Mock
    private ResultPublisher publisher;
    @InjectMocks
    private CommandProcessor processor;

    @Test
    void processesCommandThroughPortsAndPublishesItsOutcome() {
        when(operations.existsByOperationId("op-10")).thenReturn(false);
        when(operations.complete("op-10", OperationStatus.COMPLETED, "La pregunta pasó a PENDING_REVIEW"))
                .thenReturn(new OperationRecord("op-10", 10L, "SubmitQuestionForReview",
                        OperationStatus.COMPLETED, "La pregunta pasó a PENDING_REVIEW",
                        Instant.now(), Instant.now()));
        SubmitQuestionForReviewCommand command = new SubmitQuestionForReviewCommand(
                "op-10", 10L, 3L, Instant.now());

        processor.process(command);

        verify(operations).createPending("op-10", 10L, "SubmitQuestionForReview");
        verify(lifecycle).transition(10L, QuestionStatus.PENDING_REVIEW, 3L,
                "Enviada a revisión", "op-10");
        verify(publisher).publish(any(OperationResultMessage.class));
    }
}
