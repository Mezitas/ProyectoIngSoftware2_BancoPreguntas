package co.edu.unicauca.user.service;

import co.edu.unicauca.user.dto.UserDTO;
import co.edu.unicauca.user.exception.NotFoundException;
import co.edu.unicauca.user.model.UserEntity;
import co.edu.unicauca.user.repository.UserRepository;
import java.util.Optional;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /** Devuelve el usuario si las credenciales son correctas; vacío en caso contrario. */
    public Optional<UserDTO> authenticate(String username, String password) {
        return repository.findByUsername(username)
                .filter(u -> PasswordHasher.matches(password, u.getPasswordHash()))
                .map(this::toDto);
    }

    public UserDTO findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("No existe el usuario con id " + id));
    }

    private UserDTO toDto(UserEntity u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), new TreeSet<>(u.getRoles()));
    }
}
