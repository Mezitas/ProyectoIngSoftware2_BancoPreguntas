package co.edu.unicauca.review.client;

import co.edu.unicauca.review.dto.UserInfoDTO;
import co.edu.unicauca.review.exception.ServiceUnavailableException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {

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
            throw new ServiceUnavailableException("User Service no está disponible");
        }
    }
}
