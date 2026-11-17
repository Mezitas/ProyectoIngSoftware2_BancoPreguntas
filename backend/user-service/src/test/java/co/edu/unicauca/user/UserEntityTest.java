package co.edu.unicauca.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.user.model.UserEntity;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    void constructorStoresTheProfileAndCopiesTheRoles() {
        Set<String> roles = new HashSet<>(Set.of("DOCENTE"));

        UserEntity user = new UserEntity("docente1", "Ana Docente", "ana@example.edu", roles);
        roles.add("ADMIN");

        assertNull(user.getId());
        assertEquals("docente1", user.getUsername());
        assertEquals("Ana Docente", user.getFullName());
        assertEquals("ana@example.edu", user.getEmail());
        assertEquals(Set.of("DOCENTE"), user.getRoles());
    }

    @Test
    void hasRoleOnlyMatchesAssignedRoles() {
        UserEntity user = new UserEntity("rev", "Rosa", "rosa@example.edu", Set.of("REVISOR"));

        assertTrue(user.hasRole("REVISOR"));
        assertFalse(user.hasRole("ADMIN"));
    }

    @Test
    void synchronizeProfileReplacesNameEmailAndRoles() {
        UserEntity user = new UserEntity("rev", "Rosa", "rosa@example.edu", Set.of("REVISOR"));

        user.synchronizeProfile("Rosa Perez", "rosa.perez@example.edu", Set.of("ADMIN"));

        assertEquals("Rosa Perez", user.getFullName());
        assertEquals("rosa.perez@example.edu", user.getEmail());
        assertEquals(Set.of("ADMIN"), user.getRoles());
        assertFalse(user.hasRole("REVISOR"));
    }
}
