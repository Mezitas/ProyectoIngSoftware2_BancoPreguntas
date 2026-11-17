package co.edu.unicauca.review.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.review.exception.ForbiddenException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthenticatedUserTest {

    @Test
    void hasRoleChecksTheTokenRoles() {
        AuthenticatedUser admin = new AuthenticatedUser(2L, "admin1", Set.of("ADMIN"));

        assertTrue(admin.hasRole("ADMIN"));
        assertFalse(admin.hasRole("REVISOR"));
        assertFalse(new AuthenticatedUser(1L, "x", null).hasRole("ADMIN"));
    }

    @Test
    void resolveIdAcceptsNothingOrTheSameId() {
        AuthenticatedUser user = new AuthenticatedUser(3L, "revisor1", Set.of("REVISOR"));

        assertEquals(3L, user.resolveId(null, "reviewerId"));
        assertEquals(3L, user.resolveId(3L, "reviewerId"));
    }

    @Test
    void resolveIdRejectsAnotherUsersId() {
        AuthenticatedUser user = new AuthenticatedUser(3L, "revisor1", Set.of("REVISOR"));

        assertThrows(ForbiddenException.class, () -> user.resolveId(4L, "reviewerId"));
    }
}
