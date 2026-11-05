package co.edu.unicauca.review.dto;

import java.util.Set;

public record UserInfoDTO(Long id, String username, String fullName, String email, Set<String> roles) {
}
