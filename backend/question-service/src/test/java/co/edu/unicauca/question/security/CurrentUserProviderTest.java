package co.edu.unicauca.question.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.dto.UserInfoDTO;
import co.edu.unicauca.question.exception.ForbiddenException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class CurrentUserProviderTest {

    @Mock
    private UserClient userClient;
    @InjectMocks
    private CurrentUserProvider provider;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticate(String username, String... roles) {
        Jwt.Builder builder = Jwt.withTokenValue("t").header("alg", "none");
        if (username != null) {
            builder.claim("preferred_username", username);
        } else {
            builder.claim("sub", "anon");
        }
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(builder.build(),
                java.util.Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList()));
    }

    @Test
    void resolvesTheLocalProfileAndTheRolesFromTheJwt() {
        authenticate("docente1", "DOCENTE", "AUTOR");
        when(userClient.findByUsername("docente1")).thenReturn(Optional.of(
                new UserInfoDTO(7L, "docente1", "Ana", "ana@example.edu", Set.of("DOCENTE"))));

        AuthenticatedUser user = provider.current();

        assertEquals(7L, user.id());
        assertEquals("docente1", user.username());
        assertEquals(Set.of("DOCENTE", "AUTOR"), user.roles());
    }

    @Test
    void rejectsRequestsWithoutAJwt() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("x", "y"));

        assertThrows(ForbiddenException.class, () -> provider.current());
    }

    @Test
    void rejectsTokensWithoutPreferredUsername() {
        authenticate(null, "DOCENTE");

        assertThrows(ForbiddenException.class, () -> provider.current());
    }

    @Test
    void rejectsUsersWithoutLocalProfile() {
        authenticate("ghost", "DOCENTE");
        when(userClient.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> provider.current());
    }
}
