package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.config.RabbitMQConfig;
import co.edu.unicauca.question.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Recibe el resultado que Lifecycle Service publica al terminar de procesar un comando. */
@Component
public class OperationResultListener {

    private static final Logger log = LoggerFactory.getLogger(OperationResultListener.class);

    private final QuestionService service;

    public OperationResultListener(QuestionService service) {
        this.service = service;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_OPERATION_RESULT)
    public void onOperationResult(OperationResultMessage message) {
        log.info("[QuestionService] Resultado recibido: operación={} estado={}", message.operationId(), message.status());
        service.applyOperationResult(message);
    }
}
