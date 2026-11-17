package co.edu.unicauca.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import co.edu.unicauca.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void sendsARealMailMessageThroughTheSmtpSender() {
        EmailService service = new EmailService(mailSender, true, "no-reply@example.edu");

        service.send("autor@example.edu", "Asunto", "Cuerpo");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertEquals("no-reply@example.edu", sent.getFrom());
        assertEquals("autor@example.edu", sent.getTo()[0]);
        assertEquals("Asunto", sent.getSubject());
        assertEquals("Cuerpo", sent.getText());
    }

    @Test
    void doesNotSendWhenDisabled() {
        EmailService service = new EmailService(mailSender, false, "no-reply@example.edu");

        service.send("autor@example.edu", "Asunto", "Cuerpo");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void rejectsARecipientWithoutEmail() {
        EmailService service = new EmailService(mailSender, true, "no-reply@example.edu");

        assertThrows(IllegalArgumentException.class, () -> service.send(" ", "Asunto", "Cuerpo"));
        assertThrows(IllegalArgumentException.class, () -> service.send(null, "Asunto", "Cuerpo"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
