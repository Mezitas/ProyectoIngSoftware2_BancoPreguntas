package co.edu.unicauca.notification.controller;

import co.edu.unicauca.notification.model.NotificationEntity;
import co.edu.unicauca.notification.service.NotificationService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    /** Consulta las notificaciones generadas (evidencia del procesamiento asíncrono). */
    @GetMapping
    public List<NotificationDTO> list(@RequestParam(required = false) Long recipientId,
                                      Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        String email = authentication instanceof JwtAuthenticationToken jwtAuthentication
                ? jwtAuthentication.getToken().getClaimAsString("email") : null;
        return service.findVisible(recipientId, email, admin).stream().map(NotificationDTO::from).toList();
    }

    public record NotificationDTO(Long id, Long recipientId, String recipientEmail, String type, String subject,
                                  String message, String status, java.time.Instant createdAt,
                                  java.time.Instant sentAt) {
        static NotificationDTO from(NotificationEntity n) {
            return new NotificationDTO(n.getId(), n.getRecipientId(), n.getRecipientEmail(), n.getType(),
                    n.getSubject(), n.getMessage(), n.getStatus(), n.getCreatedAt(), n.getSentAt());
        }
    }
}
