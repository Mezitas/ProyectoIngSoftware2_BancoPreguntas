package co.edu.unicauca.review.repository;

import co.edu.unicauca.review.model.ReviewEntity;
import co.edu.unicauca.review.model.ReviewResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    boolean existsByQuestionIdAndResult(Long questionId, ReviewResult result);
    boolean existsByAssignmentId(Long assignmentId);
    List<ReviewEntity> findByQuestionIdOrderByIdAsc(Long questionId);
    java.util.Optional<ReviewEntity> findFirstByQuestionIdAndResultOrderByIdAsc(Long questionId, ReviewResult result);
}
