package co.edu.unicauca.lifecycle.messaging;

import co.edu.unicauca.lifecycle.config.RabbitMQConfig;
import co.edu.unicauca.lifecycle.service.CommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CommandListener {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final CommandProcessor processor;

    public CommandListener(CommandProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SUBMIT_REVIEW)
    public void handleSubmitForReview(SubmitQuestionForReviewCommand command) {
        log.info("[LifecycleService] Comando recibido: SubmitQuestionForReview pregunta={} operación={}",
                command.questionId(), command.operationId());
        processor.process(command);
    }
}
