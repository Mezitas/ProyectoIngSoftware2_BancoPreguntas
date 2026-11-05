package co.edu.unicauca.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Simula el envío (diferido) de un correo. El retardo demuestra que NO bloquea la asignación. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final long delayMs;

    public EmailService(@Value("${notification.email.delay-ms:2000}") long delayMs) {
        this.delayMs = delayMs;
    }

    public void send(String to, String subject, String body) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envío interrumpido", e);
        }
        log.info("[EmailService] Correo enviado a {} | asunto: {} | {}", to, subject, body);
    }
}
