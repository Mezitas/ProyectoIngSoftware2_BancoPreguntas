package co.edu.unicauca.notification.repository;

import co.edu.unicauca.notification.model.NotificationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    boolean existsByEventId(String eventId);

    List<NotificationEntity> findByRecipientIdOrderByIdAsc(Long recipientId);

    List<NotificationEntity> findByRecipientEmailIgnoreCaseOrderByIdAsc(String recipientEmail);
}
