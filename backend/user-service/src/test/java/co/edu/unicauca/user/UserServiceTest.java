package co.edu.unicauca.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.user.model.UserEntity;
import co.edu.unicauca.user.repository.UserRepository;
import co.edu.unicauca.user.service.UserService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;
    @InjectMocks
    private UserService service;

    @Test
    void firstAuthenticatedKeycloakIdentityIsProvisionedWithItsRealmRoles() {
        when(repository.findByUsername("new-user")).thenReturn(Optional.empty());
        when(repository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, UserEntity.class));

        var profile = service.synchronizeProfile("new-user", "New User", "new-user@example.edu",
                Set.of("DOCENTE", "AUTOR"));

        assertEquals("new-user", profile.username());
        assertEquals(Set.of("DOCENTE", "AUTOR"), profile.roles());
        verify(repository).save(any(UserEntity.class));
    }

    @Test
    void subsequentLoginUpdatesRolesFromKeycloak() {
        UserEntity existing = new UserEntity("new-user", "New User", "new-user@example.edu", Set.of("DOCENTE"));
        when(repository.findByUsername("new-user")).thenReturn(Optional.of(existing));
        when(repository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, UserEntity.class));

        var profile = service.synchronizeProfile("new-user", "New User", "new-user@example.edu", Set.of("REVISOR"));

        assertEquals(Set.of("REVISOR"), profile.roles());
        verify(repository).save(existing);
    }
}
