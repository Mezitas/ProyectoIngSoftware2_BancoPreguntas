package co.edu.unicauca.review.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_result", nullable = false)
    private ReviewResult result;

    @Column(length = 2000)
    private String comments;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected ReviewEntity() {
    }

    public ReviewEntity(Long assignmentId, Long questionId, Long reviewerId, ReviewResult result, String comments) {
        this.assignmentId = assignmentId;
        this.questionId = questionId;
        this.reviewerId = reviewerId;
        this.result = result;
        this.comments = comments;
    }

    public Long getId() { return id; }
    public Long getAssignmentId() { return assignmentId; }
    public Long getQuestionId() { return questionId; }
    public Long getReviewerId() { return reviewerId; }
    public ReviewResult getResult() { return result; }
    public String getComments() { return comments; }
    public Instant getCreatedAt() { return createdAt; }
}
