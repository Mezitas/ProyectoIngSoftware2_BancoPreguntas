package co.edu.unicauca.user.dto;

import java.util.Set;

public record UserDTO(Long id, String username, String fullName, String email, Set<String> roles) {
}
