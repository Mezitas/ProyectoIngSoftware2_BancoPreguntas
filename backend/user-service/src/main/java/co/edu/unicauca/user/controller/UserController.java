package co.edu.unicauca.user.controller;

import co.edu.unicauca.user.dto.UserDTO;
import co.edu.unicauca.user.exception.ForbiddenException;
import co.edu.unicauca.user.service.UserService;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/** Consulta de usuarios y roles; la usan Question Service y Review Service (REST síncrono). */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDTO findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /**
     * Resuelve el perfil local de un usuario a partir de su nombre de usuario de Keycloak. Los demás
     * microservicios lo usan para obtener la identidad real del JWT (claim preferred_username) en lugar
     * de confiar en un identificador enviado en el cuerpo de la petición.
     * Solo puede consultarse el propio perfil, salvo que quien llama sea ADMIN.
     */
    @GetMapping("/by-username/{username}")
    public UserDTO findByUsername(@PathVariable String username, Authentication authentication) {
        if (!isSelfOrAdmin(authentication, username)) {
            throw new ForbiddenException("Solo puede consultar su propio perfil");
        }
        return userService.findByUsername(username);
    }

    /** Lista los usuarios con un rol (p. ej. {@code GET /users?role=ADMIN}). */
    @GetMapping
    public List<UserDTO> findByRole(@RequestParam String role) {
        return userService.findByRole(role.trim().toUpperCase(Locale.ROOT));
    }

    static boolean isSelfOrAdmin(Authentication authentication, String username) {
        if (authentication == null) {
            return false;
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (admin) {
            return true;
        }
        return authentication instanceof JwtAuthenticationToken jwtAuthentication
                && username.equals(jwtAuthentication.getToken().getClaimAsString("preferred_username"));
    }
}
