package co.edu.unicauca.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import co.edu.unicauca.notification.model.NotificationEntity;
import org.junit.jupiter.api.Test;

class NotificationEntityTest {

    private static NotificationEntity sample() {
        return new NotificationEntity("evt-1", 7L, "autor@example.edu", "REVIEW_COMPLETED", "Asunto", "Mensaje");
    }

    @Test
    void newNotificationsStartPending() {
        NotificationEntity n = sample();

        assertEquals("PENDING", n.getStatus());
        assertEquals("evt-1", n.getEventId());
        assertEquals(7L, n.getRecipientId());
        assertEquals("autor@example.edu", n.getRecipientEmail());
        assertEquals("REVIEW_COMPLETED", n.getType());
        assertEquals("Asunto", n.getSubject());
        assertEquals("Mensaje", n.getMessage());
        assertNotNull(n.getCreatedAt());
        assertNull(n.getSentAt());
    }

    @Test
    void markSentRecordsTheDeliveryTime() {
        NotificationEntity n = sample();

        n.markSent();

        assertEquals("SENT", n.getStatus());
        assertNotNull(n.getSentAt());
    }

    @Test
    void markFailedKeepsNoDeliveryTime() {
        NotificationEntity n = sample();

        n.markFailed();

        assertEquals("FAILED", n.getStatus());
        assertNull(n.getSentAt());
    }
}
