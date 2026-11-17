package co.edu.unicauca.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía correos reales por SMTP (en Docker Compose, MailHog: bandeja en http://localhost:8025).
 * Si el envío falla lanza una excepción y NotificationService marca la notificación como FAILED.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${notification.email.enabled:true}") boolean enabled,
                        @Value("${notification.email.from:no-reply@saberpro.unicauca.edu.co}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("El destinatario no tiene correo electrónico");
        }
        if (!enabled) {
            log.info("[EmailService] Envío deshabilitado; correo no enviado a {} | asunto: {}", to, subject);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("[EmailService] Correo enviado a {} | asunto: {}", to, subject);
    }
}
