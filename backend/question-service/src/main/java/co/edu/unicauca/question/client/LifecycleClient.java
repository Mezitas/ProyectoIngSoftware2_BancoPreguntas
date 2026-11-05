package co.edu.unicauca.question.client;

import co.edu.unicauca.question.dto.LifecycleStateDTO;
import co.edu.unicauca.question.exception.ConflictException;
import co.edu.unicauca.question.exception.ServiceUnavailableException;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Llamadas REST síncronas a Lifecycle Service (única autoridad sobre el estado). */
@Component
public class LifecycleClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public LifecycleClient(RestTemplate rest, @Value("${services.lifecycle-url}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    /** Registra la pregunta recién creada en estado DRAFT. */
    public LifecycleStateDTO register(Long questionId, Long createdBy) {
        try {
            LifecycleStateDTO state = rest.postForObject(baseUrl + "/lifecycle",
                    Map.of("questionId", questionId, "createdBy", createdBy),
                    LifecycleStateDTO.class);
            if (state == null) {
                throw new ServiceUnavailableException("Lifecycle Service devolvió una respuesta vacía al registrar la pregunta");
            }
            return state;
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Lifecycle Service no está disponible; la pregunta no se registró");
        }
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

    public void transition(Long questionId, String targetState, Long requestedBy, String comment) {
        try {
            rest.postForObject(baseUrl + "/lifecycle/" + questionId + "/transition",
                    Map.of("targetState", targetState, "requestedBy", requestedBy, "comment", comment),
                    Object.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new ConflictException("Lifecycle Service rechazó la transición a " + targetState);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Lifecycle Service no está disponible");
        }
    }
}
