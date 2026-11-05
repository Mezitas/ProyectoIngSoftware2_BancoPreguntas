package co.edu.unicauca.review.repository;

import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.ReviewAssignmentEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignmentEntity, Long> {

    boolean existsByQuestionIdAndStatusIn(Long questionId, Collection<AssignmentStatus> statuses);

    boolean existsByQuestionIdAndReviewerIdAndStatusIn(Long questionId, Long reviewerId,
                                                        Collection<AssignmentStatus> statuses);

    long countByQuestionIdAndStatusIn(Long questionId, Collection<AssignmentStatus> statuses);

    long countByQuestionIdAndStatus(Long questionId, AssignmentStatus status);

    Optional<ReviewAssignmentEntity> findFirstByQuestionIdAndReviewerIdAndStatus(Long questionId, Long reviewerId,
                                                                               AssignmentStatus status);

    List<ReviewAssignmentEntity> findByQuestionIdOrderByIdAsc(Long questionId);

    List<ReviewAssignmentEntity> findByReviewerIdAndStatusOrderByAssignedAtAsc(Long reviewerId,
                                                                               AssignmentStatus status);

    List<ReviewAssignmentEntity> findByQuestionIdAndStatusIn(Long questionId,
                                                             Collection<AssignmentStatus> statuses);
}
