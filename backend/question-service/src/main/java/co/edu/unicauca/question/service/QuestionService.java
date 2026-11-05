
package co.edu.unicauca.question.service;

import co.edu.unicauca.question.client.LifecycleClient;
import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.client.ValidationClient;
import co.edu.unicauca.question.dto.*;
import co.edu.unicauca.question.exception.*;
import co.edu.unicauca.question.messaging.CommandPublisher;
import co.edu.unicauca.question.messaging.OperationResultMessage;
import co.edu.unicauca.question.messaging.SubmitQuestionForReviewCommand;
import co.edu.unicauca.question.model.OperationEntity;
import co.edu.unicauca.question.model.OptionEmbeddable;
import co.edu.unicauca.question.model.QuestionEntity;
import co.edu.unicauca.question.repository.OperationRepository;
import co.edu.unicauca.question.repository.QuestionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

/**
 * Orquesta el flujo de preguntas. No usa @Transactional a propósito: las llamadas remotas
 * (REST) no deben ejecutarse dentro de una transacción de base de datos abierta; cada
 * escritura local es atómica por sí sola y las fallas se compensan explícitamente.
 */
@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private static final String TYPE_SUBMIT = "SubmitQuestionForReview";
    private static final Set<String> AUTHOR_ROLES = Set.of("DOCENTE", "AUTOR");

    private final QuestionRepository questions;
    private final OperationRepository operations;
    private final ValidationClient validationClient;
    private final UserClient userClient;
    private final LifecycleClient lifecycleClient;
    private final CommandPublisher publisher;

    public QuestionService(QuestionRepository questions, OperationRepository operations,
                           ValidationClient validationClient, UserClient userClient,
                           LifecycleClient lifecycleClient, CommandPublisher publisher) {
        this.questions = questions;
        this.operations = operations;
        this.validationClient = validationClient;
        this.userClient = userClient;
        this.lifecycleClient = lifecycleClient;
        this.publisher = publisher;
    }

    // ------------------------------------------------------------------ crear
    public QuestionResponseDTO create(QuestionRequestDTO req) {
        checkAuthor(req.authorId());
        validateContent(req);                                   // REST síncrono -> Validation Service

        QuestionEntity entity = new QuestionEntity();
        entity.setAuthorId(req.authorId());
        applyContent(entity, req);
        entity = questions.save(entity);

        try {
            lifecycleClient.register(entity.getId(), req.authorId());   // REST síncrono -> Lifecycle (DRAFT)
        } catch (RuntimeException e) {
            questions.deleteById(entity.getId());                       // compensación: no queda una pregunta sin estado
            throw e;
        }
        return toResponse(entity, "DRAFT");
    }

    // ------------------------------------------------------------- consultar
    public QuestionResponseDTO get(Long id) {
        QuestionEntity entity = find(id);
        return toResponse(entity, currentStatus(entity));
    }

    public List<QuestionResponseDTO> list() {
        return questions.findAll().stream().map(q -> toResponse(q, currentStatus(q))).toList();
    }

    // -------------------------------------------------------------- corregir
    /** Edita el contenido. Solo permitido en DRAFT o REJECTED; si estaba REJECTED vuelve a DRAFT. */
    public QuestionResponseDTO update(Long id, QuestionRequestDTO req) {
        QuestionEntity entity = find(id);
        if (!entity.getAuthorId().equals(req.authorId())) {
            throw new ForbiddenException("Solo el autor de la pregunta puede modificarla");
        }
        String status = currentStatus(entity);
        if ("UNAVAILABLE".equals(status)) {
            throw new ServiceUnavailableException("Lifecycle Service no está disponible; no se puede editar la pregunta");
        }
        if (!"DRAFT".equals(status) && !"REJECTED".equals(status)) {
            throw new ConflictException("No se puede editar una pregunta en estado " + status);
        }
        validateContent(req);

        if ("REJECTED".equals(status)) {
            lifecycleClient.transition(id, "DRAFT", req.authorId(), "Pregunta corregida por el autor");
            status = "DRAFT";
        }
        applyContent(entity, req);
        return toResponse(questions.save(entity), status);
    }

    // ------------------------------------------------------ envío a revisión
    /** Publica el comando en RabbitMQ y responde sin esperar a que la transición termine. */
    public SubmitAcceptedDTO submitForReview(Long questionId, Long requestedBy) {
        QuestionEntity entity = find(questionId);
        Long by = requestedBy == null ? entity.getAuthorId() : requestedBy;

        String operationId = UUID.randomUUID().toString();
        operations.save(new OperationEntity(operationId, questionId, TYPE_SUBMIT));

        try {
            publisher.publishSubmit(new SubmitQuestionForReviewCommand(operationId, questionId, by, Instant.now()));
        } catch (AmqpException e) {
            OperationEntity op = operations.findById(operationId).orElseThrow();
            op.finish("FAILED", "No se pudo publicar el comando en RabbitMQ", null);
            operations.save(op);
            log.error("Fallo publicando comando en RabbitMQ", e);
            throw new ServiceUnavailableException("El broker de mensajería no está disponible; la solicitud no fue aceptada");
        }
        return new SubmitAcceptedDTO("Solicitud de envío a revisión aceptada para procesamiento.",
                operationId, questionId, "PENDING", "/operations/" + operationId);
    }

    public OperationDTO getOperation(String operationId) {
        return operations.findById(operationId)
                .map(o -> new OperationDTO(o.getOperationId(), o.getQuestionId(), o.getType(), o.getStatus(),
                        o.getMessage(), o.getCreatedAt(), o.getCompletedAt()))
                .orElseThrow(() -> new NotFoundException("No existe la operación " + operationId));
    }

    /** Actualiza la operación con el resultado publicado por Lifecycle Service (idempotente). */
    public void applyOperationResult(OperationResultMessage msg) {
        operations.findById(msg.operationId()).ifPresentOrElse(op -> {
            op.finish(msg.status(), msg.message(), msg.completedAt());
            operations.save(op);
        }, () -> log.warn("Resultado para una operación desconocida: {}", msg.operationId()));
    }

    // ---------------------------------------------------------------- helpers
    private void checkAuthor(Long authorId) {
        UserInfoDTO user = userClient.findById(authorId)
                .orElseThrow(() -> new ForbiddenException("El autor " + authorId + " no existe"));
        boolean allowed = user.roles() != null && user.roles().stream().anyMatch(AUTHOR_ROLES::contains);
        if (!allowed) {
            throw new ForbiddenException("El usuario " + user.username() + " no tiene rol DOCENTE/AUTOR");
        }
    }

    private void validateContent(QuestionRequestDTO req) {
        ValidationResultDTO result = validationClient.validate(new ValidationRequestDTO(req.context(), req.statement(),
                req.options(), req.correctOption(), req.justification(), req.competency(), req.topic(), req.subtopic(),
                req.bibliography(), req.difficulty()));
        if (!result.valid()) {
            throw new ValidationFailedException(result.errors());
        }
    }

    private void applyContent(QuestionEntity entity, QuestionRequestDTO req) {
        List<OptionEmbeddable> options = req.options().stream()
                .map(o -> new OptionEmbeddable(o.label().trim().toUpperCase(), o.text().trim()))
                .toList();
        entity.updateContent(req.context(), req.statement(), options, req.correctOption().trim().toUpperCase(),
                req.justification(), req.competency(), req.topic(), req.subtopic(), req.bibliography().trim(),
                normalizeDifficulty(req.difficulty()));
    }

    private static String normalizeDifficulty(String difficulty) {
        return switch (difficulty.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "BASICA", "BÁSICA", "BASIC" -> "BASIC";
            case "MEDIA", "MEDIUM" -> "MEDIUM";
            case "ALTA", "HIGH" -> "HIGH";
            default -> difficulty.trim().toUpperCase(java.util.Locale.ROOT);
        };
    }

    private QuestionEntity find(Long id) {
        return questions.findById(id).orElseThrow(() -> new NotFoundException("No existe la pregunta " + id));
    }

    private String currentStatus(QuestionEntity question) {
        try {
            return lifecycleClient.getState(question.getId())
                    .map(LifecycleStateDTO::status)
                    .orElseGet(() -> lifecycleClient.register(question.getId(), question.getAuthorId()).status());
        } catch (ServiceUnavailableException e) {
            return "UNAVAILABLE";
        }
    }

    private QuestionResponseDTO toResponse(QuestionEntity q, String status) {
        List<OptionDTO> options = q.getOptions().stream().map(o -> new OptionDTO(o.getLabel(), o.getText())).toList();
        return new QuestionResponseDTO(q.getId(), q.getAuthorId(), status, q.getContext(), q.getStatement(), options,
                q.getCorrectOption(), q.getJustification(), q.getCompetency(), q.getTopic(), q.getSubtopic(),
                q.getCreatedAt(), q.getUpdatedAt(), q.getBibliography(), q.getDifficulty());
    }
}
