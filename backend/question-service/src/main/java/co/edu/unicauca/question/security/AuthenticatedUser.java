package co.edu.unicauca.question.security;

import co.edu.unicauca.question.exception.ForbiddenException;
import java.util.Set;

/**
 * Identidad del usuario que hace la petición, obtenida del JWT validado (no del cuerpo de la petición).
 * {@code id} es el identificador del perfil local en User Service; los roles provienen del token.
 */
public record AuthenticatedUser(Long id, String username, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /** Puede crear y editar preguntas propias. */
    public boolean isAuthor() {
        return hasRole("DOCENTE") || hasRole("AUTOR");
    }

    /** Administradores y revisores pueden consultar preguntas de otros autores. */
    public boolean canSeeAll() {
        return hasRole("ADMIN") || hasRole("REVISOR");
    }

    /**
     * Devuelve el id del token. Si el cliente envió un id en el cuerpo y no coincide con el del token
     * se rechaza la petición: nadie puede actuar en nombre de otro usuario.
     */
    public Long resolveId(Long claimed, String field) {
        if (claimed != null && !claimed.equals(id)) {
            throw new ForbiddenException("El campo " + field + " no coincide con el usuario autenticado");
        }
        return id;
    }
}
