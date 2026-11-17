package co.edu.unicauca.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unicauca.notification.model.NotificationEntity;
import co.edu.unicauca.notification.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class NotificationControllerTest {

    private final NotificationService service = mock(NotificationService.class);
    private final NotificationController controller = new NotificationController(service);

    private static JwtAuthenticationToken token(String email, String... roles) {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").claim("email", email).build();
        return new JwtAuthenticationToken(jwt,
                java.util.Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
    }

    @Test
    void regularUsersAreScopedByTheEmailClaim() {
        NotificationEntity n = new NotificationEntity("e", 7L, "ana@example.edu", "T", "S", "M");
        when(service.findVisible(7L, "ana@example.edu", false)).thenReturn(List.of(n));

        var result = controller.list(7L, token("ana@example.edu", "DOCENTE"));

        assertEquals(1, result.size());
        assertEquals("ana@example.edu", result.get(0).recipientEmail());
        assertEquals("PENDING", result.get(0).status());
    }

    @Test
    void administratorsAreFlaggedAsSuch() {
        when(service.findVisible(null, "admin@example.edu", true)).thenReturn(List.of());

        assertEquals(0, controller.list(null, token("admin@example.edu", "ADMIN")).size());
    }
}
