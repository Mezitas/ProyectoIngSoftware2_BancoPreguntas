package co.edu.unicauca.review.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.dto.UserInfoDTO;
import co.edu.unicauca.review.exception.ForbiddenException;
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
        authenticate("revisor1", "REVISOR");
        when(userClient.findByUsername("revisor1")).thenReturn(Optional.of(
                new UserInfoDTO(3L, "revisor1", "Rosa", "rosa@example.edu", Set.of("REVISOR"))));

        AuthenticatedUser user = provider.current();

        assertEquals(3L, user.id());
        assertEquals(Set.of("REVISOR"), user.roles());
    }

    @Test
    void rejectsRequestsWithoutAJwt() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("x", "y"));

        assertThrows(ForbiddenException.class, () -> provider.current());
    }

    @Test
    void rejectsTokensWithoutPreferredUsername() {
        authenticate(null, "REVISOR");

        assertThrows(ForbiddenException.class, () -> provider.current());
    }

    @Test
    void rejectsUsersWithoutLocalProfile() {
        authenticate("ghost", "REVISOR");
        when(userClient.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> provider.current());
    }
}
