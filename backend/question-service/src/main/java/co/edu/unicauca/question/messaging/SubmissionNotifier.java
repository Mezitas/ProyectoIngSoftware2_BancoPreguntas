package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.dto.UserInfoDTO;
import co.edu.unicauca.question.security.AuthenticatedUser;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Avisa a los administradores cuando un autor envía (o reenvía corregida) una pregunta a revisión, para que
 * le asignen revisores. Es best-effort: un fallo de notificación nunca debe impedir el envío de la pregunta.
 */
@Component
public class SubmissionNotifier {

    private static final Logger log = LoggerFactory.getLogger(SubmissionNotifier.class);

    private final UserClient userClient;
    private final QuestionEventPublisher publisher;

    public SubmissionNotifier(UserClient userClient, QuestionEventPublisher publisher) {
        this.userClient = userClient;
        this.publisher = publisher;
    }

    public void notifyAdministrators(AuthenticatedUser author, Long questionId, boolean resubmission) {
        String type = resubmission ? "QUESTION_RESUBMITTED" : "QUESTION_SUBMITTED";
        try {
            for (UserInfoDTO admin : userClient.findByRole("ADMIN")) {
                publisher.publish(new QuestionNotificationEvent(UUID.randomUUID().toString(), type, questionId,
                        admin.id(), admin.fullName(), admin.email(), author.username(), null, Instant.now()));
            }
        } catch (RuntimeException e) {
            log.error("No se pudo notificar a los administradores el envío de la pregunta {}", questionId, e);
        }
    }
}
