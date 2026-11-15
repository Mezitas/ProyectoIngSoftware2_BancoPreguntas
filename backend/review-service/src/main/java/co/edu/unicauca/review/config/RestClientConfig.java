package co.edu.unicauca.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

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
     * Reenvía al servicio de usuarios y al de ciclo de vida el token de la solicitud vigente.
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
