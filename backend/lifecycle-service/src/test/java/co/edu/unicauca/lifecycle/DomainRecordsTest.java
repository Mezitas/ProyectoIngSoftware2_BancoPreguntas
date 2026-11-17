package co.edu.unicauca.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import co.edu.unicauca.lifecycle.domain.OperationRecord;
import co.edu.unicauca.lifecycle.domain.StateHistoryRecord;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DomainRecordsTest {

    @Test
    void stateHistoryRecordKeepsTheTransitionData() {
        Instant now = Instant.now();

        StateHistoryRecord record = new StateHistoryRecord(5L, QuestionStatus.DRAFT, QuestionStatus.PENDING_REVIEW,
                7L, "envío", "op-1", now);

        assertEquals(5L, record.questionId());
        assertEquals(QuestionStatus.DRAFT, record.fromStatus());
        assertEquals(QuestionStatus.PENDING_REVIEW, record.toStatus());
        assertEquals(7L, record.changedBy());
        assertEquals("op-1", record.operationId());
        assertEquals(now, record.changedAt());
    }

    @Test
    void operationRecordKeepsTheOutcomeOfAnAsynchronousCommand() {
        Instant created = Instant.now();

        OperationRecord record = new OperationRecord("op-1", 5L, "SubmitQuestionForReview",
                OperationStatus.PENDING, "en cola", created, null);

        assertEquals(OperationStatus.PENDING, record.status());
        assertEquals(created, record.createdAt());
        assertNull(record.completedAt());
    }

    @Test
    void lifecycleDefinesTheSixOfficialStates() {
        assertEquals(6, QuestionStatus.values().length);
        assertEquals(QuestionStatus.APPROVED, QuestionStatus.valueOf("APPROVED"));
    }
}
