package co.edu.unicauca.question.model;

import jakarta.persistence.*;
import java.time.Instant;

/** Operación asíncrona iniciada por un cliente (envío a revisión). Se actualiza con el resultado de Lifecycle. */
@Entity
@Table(name = "operations")
public class OperationEntity {

    @Id
    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "op_type", nullable = false)
    private String type;

    /** PENDING | COMPLETED | FAILED */
    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected OperationEntity() {
    }

    public OperationEntity(String operationId, Long questionId, String type) {
        this.operationId = operationId;
        this.questionId = questionId;
        this.type = type;
        this.status = "PENDING";
    }

    public void finish(String finalStatus, String msg, Instant at) {
        this.status = finalStatus;
        this.message = msg;
        this.completedAt = at == null ? Instant.now() : at;
    }

    public String getOperationId() { return operationId; }
    public Long getQuestionId() { return questionId; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}
