package co.edu.unicauca.question.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.question.exception.ForbiddenException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthenticatedUserTest {

    @Test
    void rolesClassifyTheCaller() {
        AuthenticatedUser author = new AuthenticatedUser(7L, "docente1", Set.of("DOCENTE", "AUTOR"));
        AuthenticatedUser admin = new AuthenticatedUser(2L, "admin1", Set.of("ADMIN"));
        AuthenticatedUser reviewer = new AuthenticatedUser(3L, "revisor1", Set.of("REVISOR"));
        AuthenticatedUser student = new AuthenticatedUser(5L, "estudiante1", Set.of("ESTUDIANTE"));

        assertTrue(author.isAuthor());
        assertFalse(author.canSeeAll());
        assertTrue(admin.canSeeAll());
        assertTrue(reviewer.canSeeAll());
        assertFalse(student.isAuthor());
        assertFalse(student.canSeeAll());
        assertTrue(admin.hasRole("ADMIN"));
        assertFalse(new AuthenticatedUser(1L, "x", null).hasRole("ADMIN"));
    }

    @Test
    void resolveIdReturnsTheTokenIdWhenNothingOrTheSameIdIsClaimed() {
        AuthenticatedUser user = new AuthenticatedUser(7L, "docente1", Set.of("DOCENTE"));

        assertEquals(7L, user.resolveId(null, "authorId"));
        assertEquals(7L, user.resolveId(7L, "authorId"));
    }

    @Test
    void resolveIdRejectsActingOnBehalfOfAnotherUser() {
        AuthenticatedUser user = new AuthenticatedUser(7L, "docente1", Set.of("DOCENTE"));

        assertThrows(ForbiddenException.class, () -> user.resolveId(8L, "authorId"));
    }
}
