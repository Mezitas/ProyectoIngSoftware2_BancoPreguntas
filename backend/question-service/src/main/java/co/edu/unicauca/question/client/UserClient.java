package co.edu.unicauca.question.client;

import co.edu.unicauca.question.dto.UserInfoDTO;
import co.edu.unicauca.question.exception.ServiceUnavailableException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Consultas REST síncronas a User Service (perfil por id, por nombre de usuario y por rol). */
@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final RestTemplate rest;
    private final String baseUrl;

    public UserClient(RestTemplate rest, @Value("${services.user-url}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    public Optional<UserInfoDTO> findById(Long id) {
        try {
            return Optional.ofNullable(rest.getForObject(baseUrl + "/users/" + id, UserInfoDTO.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("User Service no está disponible; no se pudo verificar al autor");
        }
    }

    /** Perfil local del usuario identificado por el claim preferred_username del JWT. */
    public Optional<UserInfoDTO> findByUsername(String username) {
        try {
            return Optional.ofNullable(rest.getForObject(baseUrl + "/users/by-username/{username}",
                    UserInfoDTO.class, username));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("User Service no está disponible; no se pudo identificar al usuario");
        }
    }

    /** Usuarios con un rol. Si User Service no responde devuelve una lista vacía (uso best-effort: notificar). */
    public List<UserInfoDTO> findByRole(String role) {
        try {
            UserInfoDTO[] users = rest.getForObject(baseUrl + "/users?role={role}", UserInfoDTO[].class, role);
            return users == null ? List.of() : List.of(users);
        } catch (RestClientException e) {
            log.warn("No se pudo consultar los usuarios con rol {}: {}", role, e.getMessage());
            return List.of();
        }
    }
}
