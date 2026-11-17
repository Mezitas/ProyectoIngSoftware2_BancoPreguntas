package co.edu.unicauca.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unicauca.user.dto.UserDTO;
import co.edu.unicauca.user.exception.ForbiddenException;
import co.edu.unicauca.user.service.UserService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserControllerTest {

    private final UserService service = mock(UserService.class);
    private final UserController controller = new UserController(service);

    private static JwtAuthenticationToken token(String username, String... roles) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("preferred_username", username).build();
        return new JwtAuthenticationToken(jwt,
                java.util.Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
    }

    @Test
    void aUserCanReadItsOwnProfile() {
        UserDTO dto = new UserDTO(1L, "docente1", "Ana", "ana@example.edu", Set.of("DOCENTE"));
        when(service.findByUsername("docente1")).thenReturn(dto);

        assertEquals(dto, controller.findByUsername("docente1", token("docente1", "DOCENTE")));
    }

    @Test
    void aUserCannotReadSomeoneElsesProfile() {
        assertThrows(ForbiddenException.class,
                () -> controller.findByUsername("admin1", token("docente1", "DOCENTE")));
    }

    @Test
    void anAdministratorCanReadAnyProfile() {
        UserDTO dto = new UserDTO(2L, "docente1", "Ana", "ana@example.edu", Set.of("DOCENTE"));
        when(service.findByUsername("docente1")).thenReturn(dto);

        assertEquals(dto, controller.findByUsername("docente1", token("admin1", "ADMIN")));
    }

    @Test
    void anonymousCallersAreNeitherSelfNorAdmin() {
        assertFalse(UserController.isSelfOrAdmin(null, "docente1"));
        assertTrue(UserController.isSelfOrAdmin(token("docente1"), "docente1"));
    }

    @Test
    void roleListingNormalizesTheRoleName() {
        UserDTO admin = new UserDTO(2L, "admin1", "Carlos", "admin1@example.edu", Set.of("ADMIN"));
        when(service.findByRole("ADMIN")).thenReturn(List.of(admin));

        assertEquals(List.of(admin), controller.findByRole(" admin "));
    }
}
