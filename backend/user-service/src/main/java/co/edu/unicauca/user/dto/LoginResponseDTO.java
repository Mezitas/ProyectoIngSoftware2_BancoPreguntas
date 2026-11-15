package co.edu.unicauca.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDTO(boolean authenticated, UserDTO user, String message, String accessToken) {
}
