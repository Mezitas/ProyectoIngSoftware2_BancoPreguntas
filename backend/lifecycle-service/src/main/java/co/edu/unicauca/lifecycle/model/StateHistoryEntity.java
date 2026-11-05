package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "state_history")
public class StateHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private QuestionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private QuestionStatus toStatus;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "comment_text", length = 1000)
    private String comment;

    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();

    protected StateHistoryEntity() {
    }

    public StateHistoryEntity(Long questionId, QuestionStatus from, QuestionStatus to, Long changedBy,
                              String comment, String operationId) {
        this.questionId = questionId;
        this.fromStatus = from;
        this.toStatus = to;
        this.changedBy = changedBy;
        this.comment = comment;
        this.operationId = operationId;
    }

    public Long getId() { return id; }
    public Long getQuestionId() { return questionId; }
    public QuestionStatus getFromStatus() { return fromStatus; }
    public QuestionStatus getToStatus() { return toStatus; }
    public Long getChangedBy() { return changedBy; }
    public String getComment() { return comment; }
    public String getOperationId() { return operationId; }
    public Instant getChangedAt() { return changedAt; }
}
