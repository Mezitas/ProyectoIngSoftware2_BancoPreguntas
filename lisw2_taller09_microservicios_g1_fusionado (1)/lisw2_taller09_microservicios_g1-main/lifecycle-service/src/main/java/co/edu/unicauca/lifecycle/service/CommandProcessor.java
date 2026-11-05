package co.edu.unicauca.lifecycle.service;

import co.edu.unicauca.lifecycle.exception.BadRequestException;
import co.edu.unicauca.lifecycle.exception.InvalidTransitionException;
import co.edu.unicauca.lifecycle.exception.NotFoundException;
import co.edu.unicauca.lifecycle.messaging.OperationResultMessage;
import co.edu.unicauca.lifecycle.messaging.ResultPublisher;
import co.edu.unicauca.lifecycle.messaging.SubmitQuestionForReviewCommand;
import co.edu.unicauca.lifecycle.model.OperationResultEntity;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.repository.OperationResultRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Procesa el comando SubmitQuestionForReview: intenta DRAFT -> PENDING_REVIEW, registra
 * el resultado (COMPLETED / FAILED) y lo publica para que Question Service lo exponga.
 * Nunca lanza excepciones de negocio hacia RabbitMQ: un fallo de negocio es un resultado
 * FAILED legítimo, no un mensaje que deba reintentarse indefinidamente.
 */
@Service
public class CommandProcessor {

    static final String TYPE = "SubmitQuestionForReview";
    private static final Logger log = LoggerFactory.getLogger(CommandProcessor.class);

    private final LifecycleService lifecycle;
    private final OperationResultRepository operations;
    private final ResultPublisher publisher;

    public CommandProcessor(LifecycleService lifecycle, OperationResultRepository operations,
                            ResultPublisher publisher) {
        this.lifecycle = lifecycle;
        this.operations = operations;
        this.publisher = publisher;
    }

    public void process(SubmitQuestionForReviewCommand cmd) {
        // Idempotencia: RabbitMQ garantiza "al menos una vez"; si el comando se redelivera, no se repite.
        if (operations.existsById(cmd.operationId())) {
            log.warn("[LifecycleService] Comando duplicado ignorado: {}", cmd.operationId());
            return;
        }
        OperationResultEntity op = operations.save(
                new OperationResultEntity(cmd.operationId(), cmd.questionId(), TYPE));

        try {
            lifecycle.transition(cmd.questionId(), QuestionStatus.PENDING_REVIEW, cmd.requestedBy(),
                    "Enviada a revisión", cmd.operationId());
            op.complete(OperationStatus.COMPLETED, "La pregunta pasó a PENDING_REVIEW");
        } catch (InvalidTransitionException | NotFoundException | BadRequestException e) {
            op.complete(OperationStatus.FAILED, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado procesando la operación {}", cmd.operationId(), e);
            op.complete(OperationStatus.FAILED, "Error interno: " + e.getClass().getSimpleName());
        }
        operations.save(op);
        log.info("[LifecycleService] Operación {} -> {} ({})", op.getOperationId(), op.getStatus(), op.getMessage());

        publisher.publish(new OperationResultMessage(op.getOperationId(), op.getQuestionId(), TYPE,
                op.getStatus().name(), op.getMessage(), Instant.now()));
    }
}
