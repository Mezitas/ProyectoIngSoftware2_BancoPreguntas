package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.*;
import java.time.Instant;

/** Registro del resultado de cada comando asíncrono procesado (permite consultarlo con Postman). */
@Entity
@Table(name = "operation_results")
public class OperationResultEntity {

    @Id
    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "op_type", nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;

    @Column(length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected OperationResultEntity() {
    }

    public OperationResultEntity(String operationId, Long questionId, String type) {
        this.operationId = operationId;
        this.questionId = questionId;
        this.type = type;
        this.status = OperationStatus.PENDING;
    }

    public void complete(OperationStatus finalStatus, String msg) {
        this.status = finalStatus;
        this.message = msg;
        this.completedAt = Instant.now();
    }

    public String getOperationId() { return operationId; }
    public Long getQuestionId() { return questionId; }
    public String getType() { return type; }
    public OperationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}
