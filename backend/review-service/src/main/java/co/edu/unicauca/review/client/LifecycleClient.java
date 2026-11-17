package co.edu.unicauca.review.client;

import co.edu.unicauca.review.dto.LifecycleStateDTO;
import co.edu.unicauca.review.exception.ConflictException;
import co.edu.unicauca.review.exception.ForbiddenException;
import co.edu.unicauca.review.exception.ServiceUnavailableException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** REST síncrono hacia Lifecycle Service: consulta el estado y SOLICITA transiciones (nunca las aplica). */
@Component
public class LifecycleClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public LifecycleClient(RestTemplate rest, @Value("${services.lifecycle-url}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    public Optional<LifecycleStateDTO> getState(Long questionId) {
        try {
            return Optional.ofNullable(rest.getForObject(baseUrl + "/lifecycle/" + questionId, LifecycleStateDTO.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Lifecycle Service no está disponible");
        }
    }

    /** @throws ConflictException si Lifecycle rechaza la transición; ServiceUnavailableException si no responde. */
    public void transition(Long questionId, String targetState, Long requestedBy, String comment) {
        Map<String, Object> body = new HashMap<>();
        body.put("targetState", targetState);
        body.put("requestedBy", requestedBy);
        body.put("comment", comment);
        try {
            rest.postForObject(baseUrl + "/lifecycle/" + questionId + "/transition", body, Object.class);
        } catch (HttpClientErrorException.Forbidden e) {
            throw new ForbiddenException("Su rol no permite la transición a " + targetState);
        } catch (HttpClientErrorException e) {
            throw new ConflictException("Lifecycle Service rechazó la transición a " + targetState + ": "
                    + e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Lifecycle Service no está disponible; la transición no se realizó");
        }
    }
}
