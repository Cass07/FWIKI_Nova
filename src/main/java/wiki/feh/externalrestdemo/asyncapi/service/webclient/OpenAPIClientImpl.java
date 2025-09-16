package wiki.feh.externalrestdemo.asyncapi.service.webclient;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.util.JsonTokenManager;

import java.net.URI;
import java.util.Map;


@AllArgsConstructor
public class OpenAPIClientImpl {
    private final WebClient.Builder webClientBuilder;
    private final String BASE_URL;

    public <T> Mono<ResponseEntity<T>> post(String requestUrl, Map<String, String> headers, Object requestBody, Class<T> responseType) {
        return webClientBuilder.build()
                .method(HttpMethod.POST)
                .uri(getWebClientURI(requestUrl))
                .headers(httpHeaders -> {
                    if (headers != null) {
                        headers.forEach(httpHeaders::set);
                    }
                })
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .toEntity(responseType);
    }

    public <T> Mono<ResponseEntity<T>> get(String requestUrl, Map<String, String> headers, Object requestBody, Class<T> responseType) {
        return webClientBuilder.build()
                .method(HttpMethod.GET)
                .uri(getWebClientURI(requestUrl))
                .headers(httpHeaders -> {
                    if (headers != null) {
                        headers.forEach(httpHeaders::set);
                    }
                })
                .retrieve()
                .toEntity(responseType);
    }

    private URI getWebClientURI(String requestUrl) {
        return new DefaultUriBuilderFactory(BASE_URL)
                .uriString(requestUrl)
                .build();
    }

    private URI getWebClientURI(String requestUrl, MultiValueMap<String, String> queryParams) {
        return new DefaultUriBuilderFactory(BASE_URL)
                .uriString(requestUrl)
                .queryParams(queryParams)
                .build();
    }
}
