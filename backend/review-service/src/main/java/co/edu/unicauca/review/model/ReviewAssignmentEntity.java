package co.edu.unicauca.review.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "review_assignments")
public class ReviewAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected ReviewAssignmentEntity() {
    }

    public ReviewAssignmentEntity(Long questionId, Long reviewerId, Long assignedBy) {
        this.questionId = questionId;
        this.reviewerId = reviewerId;
        this.assignedBy = assignedBy;
    }

    public void changeStatus(AssignmentStatus newStatus, String reason) {
        this.status = newStatus;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getQuestionId() { return questionId; }
    public Long getReviewerId() { return reviewerId; }
    public Long getAssignedBy() { return assignedBy; }
    public AssignmentStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
