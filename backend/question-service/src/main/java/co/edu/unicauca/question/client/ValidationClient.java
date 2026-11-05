package co.edu.unicauca.question.client;

import co.edu.unicauca.question.dto.ValidationRequestDTO;
import co.edu.unicauca.question.dto.ValidationResultDTO;
import co.edu.unicauca.question.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Llamada REST síncrona a Validation Service (POST /validate). */
@Component
public class ValidationClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public ValidationClient(RestTemplate rest, @Value("${services.validation-url}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    public ValidationResultDTO validate(ValidationRequestDTO request) {
        try {
            ValidationResultDTO result = rest.postForObject(baseUrl + "/validate", request, ValidationResultDTO.class);
            if (result == null) {
                throw new ServiceUnavailableException("Validation Service devolvió una respuesta vacía");
            }
            return result;
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Validation Service no está disponible; no se pudo validar la pregunta");
        }
    }
}
