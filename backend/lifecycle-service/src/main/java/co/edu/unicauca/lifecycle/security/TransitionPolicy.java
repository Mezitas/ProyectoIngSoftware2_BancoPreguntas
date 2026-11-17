package co.edu.unicauca.lifecycle.security;

import co.edu.unicauca.lifecycle.exception.ForbiddenException;
import java.util.Collection;
import java.util.Set;

/**
 * Qué rol puede solicitar cada transición por la API REST, para que, por ejemplo, un autor no pueda aprobar su
 * propia pregunta llamando directamente a Lifecycle Service.
 */
public final class TransitionPolicy {

    private TransitionPolicy() {
    }

    public static void check(String targetState, Collection<String> roles) {
        Set<String> allowed = switch (targetState) {
            case "DRAFT", "PENDING_REVIEW" -> Set.of("DOCENTE", "AUTOR");
            case "IN_REVIEW", "PUBLISHED" -> Set.of("ADMIN");
            case "APPROVED", "REJECTED" -> Set.of("REVISOR", "ADMIN");
            default -> Set.of();
        };
        if (roles.stream().noneMatch(allowed::contains)) {
            throw new ForbiddenException("Su rol no permite solicitar la transición a " + targetState);
        }
    }
}
