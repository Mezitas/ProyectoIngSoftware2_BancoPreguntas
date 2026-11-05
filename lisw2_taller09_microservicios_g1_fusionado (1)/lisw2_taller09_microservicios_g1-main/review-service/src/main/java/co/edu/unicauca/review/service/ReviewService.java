package co.edu.unicauca.review.service;

import co.edu.unicauca.review.client.LifecycleClient;
import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.dto.*;
import co.edu.unicauca.review.exception.*;
import co.edu.unicauca.review.messaging.EventPublisher;
import co.edu.unicauca.review.messaging.ReviewCompletedEvent;
import co.edu.unicauca.review.messaging.ReviewerAssignedEvent;
import co.edu.unicauca.review.model.*;
import co.edu.unicauca.review.repository.ReviewAssignmentRepository;
import co.edu.unicauca.review.repository.ReviewRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registra asignaciones y resultados de revisión. NUNCA cambia el estado de la pregunta
 * por su cuenta: lo solicita a Lifecycle Service después del último voto.
 *
 * Consistencia asignación/estado (sin transacciones distribuidas):
 *   1) se guarda la asignación como PENDING;
 *   2) se solicita PENDING_REVIEW -> IN_REVIEW a Lifecycle;
 *   3) si Lifecycle confirma -> CONFIRMED y recién entonces se publica ReviewerAssigned;
 *   4) si Lifecycle rechaza o no responde -> FAILED (compensación): nunca queda una
 *      asignación activa para una pregunta que no está IN_REVIEW.
 */
@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private static final List<AssignmentStatus> ACTIVE_STATUSES =
            List.of(AssignmentStatus.PENDING, AssignmentStatus.CONFIRMED);
    private static final int MAX_REVIEWERS = 3;

    private final ReviewAssignmentRepository assignments;
    private final ReviewRepository reviews;
    private final UserClient userClient;
    private final LifecycleClient lifecycleClient;
    private final EventPublisher publisher;

    public ReviewService(ReviewAssignmentRepository assignments, ReviewRepository reviews, UserClient userClient,
                         LifecycleClient lifecycleClient, EventPublisher publisher) {
        this.assignments = assignments;
        this.reviews = reviews;
        this.userClient = userClient;
        this.lifecycleClient = lifecycleClient;
        this.publisher = publisher;
    }

    // ------------------------------------------------------------ asignar
    public AssignmentDTO assign(AssignRequestDTO req) {
        UserInfoDTO admin = userClient.findById(req.assignedBy())
                .orElseThrow(() -> new ForbiddenException("El usuario " + req.assignedBy() + " no existe"));
        if (!hasRole(admin, "ADMIN")) {
            throw new ForbiddenException("Solo un administrador puede asignar revisores");
        }

        UserInfoDTO reviewer = userClient.findById(req.reviewerId())
                .orElseThrow(() -> new UnprocessableException("El revisor " + req.reviewerId() + " no existe"));
        if (!hasRole(reviewer, "REVISOR")) {
            throw new UnprocessableException("El usuario " + reviewer.username() + " no tiene rol REVISOR");
        }

        LifecycleStateDTO state = lifecycleClient.getState(req.questionId())
                .orElseThrow(() -> new NotFoundException("La pregunta " + req.questionId() + " no existe"));
        if (!"PENDING_REVIEW".equals(state.status()) && !"IN_REVIEW".equals(state.status())) {
            throw new ConflictException("La pregunta no es elegible: está en " + state.status()
                    + " y debe estar en PENDING_REVIEW o IN_REVIEW");
        }
        if (req.reviewerId().equals(state.createdBy())) {
            throw new UnprocessableException("El autor de la pregunta no puede ser su propio revisor");
        }
        if (assignments.existsByQuestionIdAndReviewerIdAndStatusIn(req.questionId(), req.reviewerId(),
                ACTIVE_STATUSES)) {
            throw new ConflictException("Este revisor ya está asignado a la pregunta");
        }
        long activeAssignments = assignments.countByQuestionIdAndStatusIn(req.questionId(), ACTIVE_STATUSES);
        if (activeAssignments >= MAX_REVIEWERS) {
            throw new ConflictException("Una pregunta puede tener como máximo 3 revisores");
        }

        ReviewAssignmentEntity assignment = assignments.save(
                new ReviewAssignmentEntity(req.questionId(), req.reviewerId(), req.assignedBy()));

        try {
            if ("PENDING_REVIEW".equals(state.status())) {
                lifecycleClient.transition(req.questionId(), "IN_REVIEW", req.assignedBy(),
                        "Revisor asignado: " + reviewer.username());
            }
        } catch (RuntimeException e) {
            assignment.changeStatus(AssignmentStatus.FAILED, e.getMessage());
            assignments.save(assignment);
            throw e;
        }

        assignment.changeStatus(AssignmentStatus.CONFIRMED, null);
        assignments.save(assignment);

        String notification = "QUEUED";
        try {
            publisher.publishReviewerAssigned(new ReviewerAssignedEvent(UUID.randomUUID().toString(),
                    req.questionId(), reviewer.id(), reviewer.fullName(), reviewer.email(), req.assignedBy(),
                    Instant.now()));
        } catch (RuntimeException e) {
            // La asignación ya es válida; una notificación fallida no debe deshacerla.
            notification = "NOT_QUEUED";
            log.error("No se pudo publicar ReviewerAssigned para la asignación {}", assignment.getId(), e);
        }
        return toDto(assignment, notification);
    }

    // ----------------------------------------------------------- resultado
    public ReviewDTO submitResult(ReviewResultRequestDTO req) {
        if (req.result() == ReviewResult.REJECTED && (req.comments() == null || req.comments().isBlank())) {
            throw new BadRequestException("Para rechazar una pregunta debe registrar el motivo (comments)");
        }
        ReviewAssignmentEntity assignment = assignments
                .findFirstByQuestionIdAndReviewerIdAndStatus(req.questionId(), req.reviewerId(), AssignmentStatus.CONFIRMED)
                .orElseThrow(() -> new ConflictException(
                        "No existe una asignación activa de ese revisor para la pregunta " + req.questionId()));

        if (reviews.existsByAssignmentId(assignment.getId())) {
            throw new ConflictException("Este revisor ya registró su evaluación para la pregunta");
        }
        long remainingAssignments = assignments.countByQuestionIdAndStatus(
                req.questionId(), AssignmentStatus.CONFIRMED);
        boolean lastReviewer = remainingAssignments == 1;
        String target = "IN_REVIEW";
        String decisionComments = req.comments();
        if (lastReviewer) {
            boolean rejectionExists = req.result() == ReviewResult.REJECTED
                    || reviews.existsByQuestionIdAndResult(req.questionId(), ReviewResult.REJECTED);
            target = rejectionExists ? "REJECTED" : "APPROVED";
            if (rejectionExists && req.result() != ReviewResult.REJECTED) {
                decisionComments = reviews.findFirstByQuestionIdAndResultOrderByIdAsc(
                                req.questionId(), ReviewResult.REJECTED)
                        .map(ReviewEntity::getComments)
                        .orElse("Pregunta rechazada en la evaluación por pares");
            }
            lifecycleClient.transition(req.questionId(), target, req.reviewerId(), decisionComments);
        }

        ReviewEntity review = reviews.save(new ReviewEntity(assignment.getId(), req.questionId(), req.reviewerId(),
                req.result(), req.comments()));
        assignment.changeStatus(AssignmentStatus.COMPLETED, null);
        assignments.save(assignment);
        if (lastReviewer) {
            notifyParticipants(req.questionId(), assignment, target, decisionComments);
        }
        return new ReviewDTO(review.getId(), assignment.getId(), review.getQuestionId(), review.getReviewerId(),
                review.getResult(), review.getComments(), target, review.getCreatedAt());
    }

    // ------------------------------------------------------------ consultas
    public List<AssignmentDTO> findByQuestion(Long questionId) {
        return assignments.findByQuestionIdOrderByIdAsc(questionId).stream().map(a -> toDto(a, null)).toList();
    }

    public List<AssignmentDTO> findByReviewer(Long reviewerId) {
        return assignments.findByReviewerIdAndStatusOrderByAssignedAtAsc(reviewerId, AssignmentStatus.CONFIRMED)
                .stream().map(a -> toDto(a, null)).toList();
    }

    public List<ReviewDTO> findResultsByQuestion(Long questionId) {
        return reviews.findByQuestionIdOrderByIdAsc(questionId).stream()
                .map(review -> new ReviewDTO(review.getId(), review.getAssignmentId(), review.getQuestionId(),
                        review.getReviewerId(), review.getResult(), review.getComments(), null,
                        review.getCreatedAt()))
                .toList();
    }

    public AssignmentDTO findAssignment(Long id) {
        return assignments.findById(id).map(a -> toDto(a, null))
                .orElseThrow(() -> new NotFoundException("No existe la asignación " + id));
    }

    private static boolean hasRole(UserInfoDTO user, String role) {
        return user.roles() != null && user.roles().contains(role);
    }

    private static AssignmentDTO toDto(ReviewAssignmentEntity a, String notification) {
        return new AssignmentDTO(a.getId(), a.getQuestionId(), a.getReviewerId(), a.getAssignedBy(), a.getStatus(),
                a.getFailureReason(), a.getAssignedAt(), notification);
    }

    private void notifyParticipants(Long questionId, ReviewAssignmentEntity completedAssignment,
                                    String result, String comments) {
        LifecycleStateDTO state = lifecycleClient.getState(questionId)
                .orElseThrow(() -> new NotFoundException("La pregunta " + questionId + " no existe"));
        java.util.Set<Long> recipientIds = new java.util.LinkedHashSet<>();
        recipientIds.add(state.createdBy());
        recipientIds.add(completedAssignment.getAssignedBy());
        assignments.findByQuestionIdAndStatusIn(questionId,
                        List.of(AssignmentStatus.COMPLETED, AssignmentStatus.CONFIRMED))
                .forEach(a -> {
                    recipientIds.add(a.getReviewerId());
                    recipientIds.add(a.getAssignedBy());
                });
        recipientIds.remove(null);
        for (Long recipientId : recipientIds) {
            Optional<UserInfoDTO> recipient;
            try {
                recipient = userClient.findById(recipientId);
            } catch (RuntimeException e) {
                log.error("No se pudo consultar el usuario {} para notificar el resultado de la pregunta {}",
                        recipientId, questionId, e);
                continue;
            }
            if (recipient.isEmpty()) {
                log.error("No se encontró el usuario {} para notificar el resultado de la pregunta {}",
                        recipientId, questionId);
                continue;
            }
            UserInfoDTO user = recipient.get();
            try {
                publisher.publishReviewCompleted(new ReviewCompletedEvent(
                        UUID.randomUUID().toString(), questionId, recipientId, user.fullName(), user.email(),
                        result, comments, Instant.now()));
            } catch (RuntimeException e) {
                log.error("No se pudo publicar notificación de resultado para el usuario {} de la pregunta {}",
                        recipientId, questionId, e);
            }
        }
    }
}
