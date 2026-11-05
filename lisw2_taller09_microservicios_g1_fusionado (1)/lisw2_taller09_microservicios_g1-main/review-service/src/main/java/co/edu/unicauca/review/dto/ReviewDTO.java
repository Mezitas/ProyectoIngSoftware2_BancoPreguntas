package co.edu.unicauca.review.dto;

import co.edu.unicauca.review.model.ReviewResult;
import java.time.Instant;

public record ReviewDTO(Long id, Long assignmentId, Long questionId, Long reviewerId, ReviewResult result,
                        String comments, String questionStatus, Instant createdAt) {
}
