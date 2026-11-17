package co.edu.unicauca.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.user.exception.NotFoundException;
import co.edu.unicauca.user.model.UserEntity;
import co.edu.unicauca.user.repository.UserRepository;
import co.edu.unicauca.user.service.UserService;
import java.util.List;
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

    @Test
    void findByUsernameReturnsTheLocalProfile() {
        UserEntity existing = new UserEntity("docente1", "Ana Docente", "docente1@example.edu", Set.of("DOCENTE"));
        when(repository.findByUsername("docente1")).thenReturn(Optional.of(existing));

        var profile = service.findByUsername("docente1");

        assertEquals("docente1", profile.username());
        assertEquals("Ana Docente", profile.fullName());
        assertEquals(Set.of("DOCENTE"), profile.roles());
    }

    @Test
    void findByUsernameFailsWhenTheProfileDoesNotExist() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findByUsername("ghost"));
    }

    @Test
    void findByIdFailsWhenTheProfileDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void findByRoleMapsEveryUserWithThatRole() {
        UserEntity admin = new UserEntity("admin1", "Carlos Admin", "admin1@example.edu", Set.of("ADMIN"));
        when(repository.findByRole("ADMIN")).thenReturn(List.of(admin));

        var admins = service.findByRole("ADMIN");

        assertEquals(1, admins.size());
        assertEquals("admin1@example.edu", admins.get(0).email());
    }
}
