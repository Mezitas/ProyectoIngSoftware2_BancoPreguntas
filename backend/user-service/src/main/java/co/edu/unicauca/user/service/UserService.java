package co.edu.unicauca.user.service;

import co.edu.unicauca.user.dto.UserDTO;
import co.edu.unicauca.user.exception.NotFoundException;
import co.edu.unicauca.user.model.UserEntity;
import co.edu.unicauca.user.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserDTO findByUsername(String username) {
        return repository.findByUsername(username).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + username));
    }

    public List<UserDTO> findByRole(String role) {
        return repository.findByRole(role).stream().map(this::toDto).toList();
    }

    public UserDTO findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("No existe el usuario con id " + id));
    }

    /**
     * Keeps the local numeric profile identifier in sync with claims from a verified Keycloak token.
     * New Keycloak users are provisioned on their first successful sign-in.
     */
    public UserDTO synchronizeProfile(String username, String fullName, String email, Set<String> roles) {
        UserEntity user = repository.findByUsername(username)
                .orElseGet(() -> new UserEntity(username, fullName, email, roles));
        user.synchronizeProfile(fullName, email, roles);
        return toDto(repository.save(user));
    }

    private UserDTO toDto(UserEntity u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), new TreeSet<>(u.getRoles()));
    }
}
