package co.edu.unicauca.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.unicauca.lifecycle.exception.ForbiddenException;
import co.edu.unicauca.lifecycle.security.TransitionPolicy;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransitionPolicyTest {

    @Test
    void authorsMayReturnToDraftAndSubmitForReview() {
        assertDoesNotThrow(() -> TransitionPolicy.check("DRAFT", List.of("DOCENTE")));
        assertDoesNotThrow(() -> TransitionPolicy.check("PENDING_REVIEW", List.of("AUTOR")));
    }

    @Test
    void onlyAdministratorsStartReviewAndPublish() {
        assertDoesNotThrow(() -> TransitionPolicy.check("IN_REVIEW", List.of("ADMIN")));
        assertDoesNotThrow(() -> TransitionPolicy.check("PUBLISHED", List.of("ADMIN")));
        assertThrows(ForbiddenException.class, () -> TransitionPolicy.check("IN_REVIEW", List.of("DOCENTE")));
        assertThrows(ForbiddenException.class, () -> TransitionPolicy.check("PUBLISHED", List.of("REVISOR")));
    }

    @Test
    void reviewersDecideButAuthorsCannotApproveTheirOwnQuestion() {
        assertDoesNotThrow(() -> TransitionPolicy.check("APPROVED", List.of("REVISOR")));
        assertDoesNotThrow(() -> TransitionPolicy.check("REJECTED", List.of("REVISOR")));
        assertThrows(ForbiddenException.class,
                () -> TransitionPolicy.check("APPROVED", List.of("DOCENTE", "AUTOR")));
    }

    @Test
    void unknownTargetsAndRolelessCallersAreRejected() {
        assertThrows(ForbiddenException.class, () -> TransitionPolicy.check("ARCHIVED", List.of("ADMIN")));
        assertThrows(ForbiddenException.class, () -> TransitionPolicy.check("DRAFT", List.of()));
    }
}
