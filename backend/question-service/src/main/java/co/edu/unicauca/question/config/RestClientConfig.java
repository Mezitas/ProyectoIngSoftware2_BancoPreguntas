package co.edu.unicauca.question.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    /** RestTemplate con timeouts: un servicio caído o lento no debe bloquear indefinidamente a este. */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(forwardAuthorizationHeader());
        return restTemplate;
    }

    /**
     * Propaga el token del usuario al invocar servicios internos, conservando su identidad
     * y evitando que las llamadas REST entre microservicios omitan la autenticación.
     */
    private ClientHttpRequestInterceptor forwardAuthorizationHeader() {
        return (request, body, execution) -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                String authorization = attributes.getRequest().getHeader("Authorization");
                if (authorization != null && !authorization.isBlank()) {
                    request.getHeaders().set("Authorization", authorization);
                }
            }
            return execution.execute(request, body);
        };
    }
}
