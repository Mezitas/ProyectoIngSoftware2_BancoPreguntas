package co.edu.unicauca.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Adaptador de salida que delega la autenticación y emisión de access tokens a Keycloak.
 * Las contraseñas no se validan ni se almacenan en este servicio.
 */
@Service
public class KeycloakTokenService {

    private final RestTemplate http;
    private final String tokenUrl;
    private final String clientId;
    private final JwtDecoder jwtDecoder;

    public KeycloakTokenService(
            @Value("${keycloak.token-url}") String tokenUrl,
            @Value("${keycloak.client-id}") String clientId,
            JwtDecoder jwtDecoder) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.jwtDecoder = jwtDecoder;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.http = new RestTemplate(factory);
    }

    /**
     * Solicita un token usando el flujo de credenciales de usuario configurado para el cliente de escritorio.
     *
     * @return access token cuando Keycloak acepta las credenciales; vacío para un rechazo HTTP 400/401
     */
    public Optional<AuthenticatedIdentity> authenticate(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", username);
        form.add("password", password);
        form.add("scope", "openid profile email");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            JsonNode response = http.postForObject(tokenUrl, new HttpEntity<>(form, headers), JsonNode.class);
            JsonNode accessToken = response == null ? null : response.get("access_token");
            if (accessToken == null || !accessToken.isTextual() || accessToken.asText().isBlank()) {
                return Optional.empty();
            }
            String value = accessToken.asText();
            Jwt verifiedToken = jwtDecoder.decode(value);
            String authenticatedUsername = verifiedToken.getClaimAsString("preferred_username");
            String fullName = verifiedToken.getClaimAsString("name");
            String email = verifiedToken.getClaimAsString("email");
            if (authenticatedUsername == null || authenticatedUsername.isBlank()) {
                return Optional.empty();
            }
            if (fullName == null || fullName.isBlank()) {
                fullName = authenticatedUsername;
            }
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("El token de Keycloak no incluye el correo electrónico del usuario");
            }
            return Optional.of(new AuthenticatedIdentity(value, authenticatedUsername, fullName, email,
                    realmRoles(verifiedToken)));
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 400 || exception.getStatusCode().value() == 401) {
                return Optional.empty();
            }
            throw exception;
        }
    }

    private Set<String> realmRoles(Jwt jwt) {
        Object realmAccessClaim = jwt.getClaims().get("realm_access");
        if (!(realmAccessClaim instanceof Map<?, ?> realmAccess)
                || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return Set.of();
        }
        Set<String> names = new TreeSet<>();
        roles.stream().filter(String.class::isInstance).map(String.class::cast).forEach(names::add);
        return Set.copyOf(names);
    }

    /** Verified Keycloak identity details used to create or update the local user profile. */
    public record AuthenticatedIdentity(String accessToken, String username, String fullName,
                                        String email, Set<String> roles) {
    }
}
