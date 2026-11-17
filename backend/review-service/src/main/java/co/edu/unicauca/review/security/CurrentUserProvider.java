package co.edu.unicauca.review.security;

import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.dto.UserInfoDTO;
import co.edu.unicauca.review.exception.ForbiddenException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Resuelve quién llama: claim preferred_username del JWT -> perfil local (User Service). */
@Component
public class CurrentUserProvider {

    private final UserClient userClient;

    public CurrentUserProvider(UserClient userClient) {
        this.userClient = userClient;
    }

    public AuthenticatedUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new ForbiddenException("Se requiere un token JWT válido");
        }
        String username = jwtAuthentication.getToken().getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            throw new ForbiddenException("El token no identifica al usuario (preferred_username)");
        }
        UserInfoDTO profile = userClient.findByUsername(username).orElseThrow(() -> new ForbiddenException(
                "El usuario " + username + " no tiene perfil local; inicie sesión en /auth/login primero"));
        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .collect(Collectors.toSet());
        return new AuthenticatedUser(profile.id(), username, roles);
    }
}
