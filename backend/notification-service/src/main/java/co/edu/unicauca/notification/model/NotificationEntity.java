package co.edu.unicauca.notification.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador del evento origen: garantiza que un evento reentregado no genere dos correos. */
    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "notification_type", nullable = false)
    private String type;

    private String subject;

    @Column(length = 2000)
    private String message;

    /** PENDING | SENT | FAILED */
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "sent_at")
    private Instant sentAt;

    protected NotificationEntity() {
    }

    public NotificationEntity(String eventId, Long recipientId, String recipientEmail, String type,
                              String subject, String message) {
        this.eventId = eventId;
        this.recipientId = recipientId;
        this.recipientEmail = recipientEmail;
        this.type = type;
        this.subject = subject;
        this.message = message;
    }

    public void markSent() {
        this.status = "SENT";
        this.sentAt = Instant.now();
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public Long getRecipientId() { return recipientId; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getType() { return type; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSentAt() { return sentAt; }
}
