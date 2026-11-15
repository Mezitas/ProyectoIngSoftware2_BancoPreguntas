package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.*;
import java.time.Instant;

/** Estado vigente de una pregunta. Este servicio es la ÚNICA autoridad sobre este dato. */
@Entity
@Table(name = "question_states")
public class QuestionStateEntity {

    @Id
    @Column(name = "question_id")
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionStatus status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "comment_text", length = 1000)
    private String comment;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    /** Control de concurrencia optimista: dos transiciones simultáneas no se pisan. */
    @Version
    private Long version;

    protected QuestionStateEntity() {
    }

    public QuestionStateEntity(Long questionId, QuestionStatus status, Long createdBy) {
        this.questionId = questionId;
        this.status = status;
        this.createdBy = createdBy;
        this.changedBy = createdBy;
        this.changedAt = Instant.now();
    }

    /** Reconstitutes a stored state without changing its audit timestamp. */
    public QuestionStateEntity(Long questionId, QuestionStatus status, Long createdBy, Long changedBy,
                               String comment, Instant changedAt) {
        this.questionId = questionId;
        this.status = status;
        this.createdBy = createdBy;
        this.changedBy = changedBy;
        this.comment = comment;
        this.changedAt = changedAt;
    }

    public void apply(QuestionStatus newStatus, Long by, String commentText) {
        apply(newStatus, by, commentText, Instant.now());
    }

    /** Applies a domain transition while preserving its timestamp. */
    public void apply(QuestionStatus newStatus, Long by, String commentText, Instant timestamp) {
        this.status = newStatus;
        this.changedBy = by;
        this.comment = commentText;
        this.changedAt = timestamp;
    }

    public Long getQuestionId() { return questionId; }
    public QuestionStatus getStatus() { return status; }
    public Long getCreatedBy() { return createdBy; }
    public Long getChangedBy() { return changedBy; }
    public String getComment() { return comment; }
    public Instant getChangedAt() { return changedAt; }
}
