package co.edu.unicauca.lifecycle.service;

import co.edu.unicauca.lifecycle.exception.BadRequestException;
import co.edu.unicauca.lifecycle.exception.InvalidTransitionException;
import co.edu.unicauca.lifecycle.exception.NotFoundException;
import co.edu.unicauca.lifecycle.domain.OperationRecord;
import co.edu.unicauca.lifecycle.messaging.OperationResultMessage;
import co.edu.unicauca.lifecycle.messaging.ResultPublisher;
import co.edu.unicauca.lifecycle.messaging.SubmitQuestionForReviewCommand;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.port.OperationRecordStore;
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
    private final OperationRecordStore operations;
    private final ResultPublisher publisher;

    public CommandProcessor(LifecycleService lifecycle, OperationRecordStore operations,
                            ResultPublisher publisher) {
        this.lifecycle = lifecycle;
        this.operations = operations;
        this.publisher = publisher;
    }

    public void process(SubmitQuestionForReviewCommand cmd) {
        // Idempotencia: RabbitMQ garantiza "al menos una vez"; si el comando se redelivera, no se repite.
        if (operations.existsByOperationId(cmd.operationId())) {
            log.warn("[LifecycleService] Comando duplicado ignorado: {}", cmd.operationId());
            return;
        }
        operations.createPending(cmd.operationId(), cmd.questionId(), TYPE);
        OperationStatus status;
        String message;

        try {
            lifecycle.transition(cmd.questionId(), QuestionStatus.PENDING_REVIEW, cmd.requestedBy(),
                    "Enviada a revisión", cmd.operationId());
            status = OperationStatus.COMPLETED;
            message = "La pregunta pasó a PENDING_REVIEW";
        } catch (InvalidTransitionException | NotFoundException | BadRequestException e) {
            status = OperationStatus.FAILED;
            message = e.getMessage();
        } catch (Exception e) {
            log.error("Error inesperado procesando la operación {}", cmd.operationId(), e);
            status = OperationStatus.FAILED;
            message = "Error interno: " + e.getClass().getSimpleName();
        }
        OperationRecord operation = operations.complete(cmd.operationId(), status, message);
        log.info("[LifecycleService] Operación {} -> {} ({})", operation.operationId(),
                operation.status(), operation.message());

        publisher.publish(new OperationResultMessage(operation.operationId(), operation.questionId(), TYPE,
                operation.status().name(), operation.message(), Instant.now()));
    }
}
