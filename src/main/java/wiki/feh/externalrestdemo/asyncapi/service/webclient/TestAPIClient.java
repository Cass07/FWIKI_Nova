package wiki.feh.externalrestdemo.asyncapi.service.webclient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.util.JsonTokenManager;

import java.util.HashMap;
import java.util.Map;

public class TestAPIClient extends OpenAPIClientImpl {
    JsonTokenManager jsonTokenManager;

    // json token의 경우 호출 시 생성해야한다
    public TestAPIClient(WebClient.Builder webClientBuilder, String BASE_URL, JsonTokenManager jsonTokenManager) {
        super(webClientBuilder, BASE_URL);
        this.jsonTokenManager = jsonTokenManager;
    }

    @Override
    public <T> Mono<ResponseEntity<T>> get(String requestUrl, Map<String, String> headers, Object requestBody, Class<T> responseType) {
        return super.get(
                requestUrl,
                addJsonTokenToHeaders(headers),
                requestBody,
                responseType
        );
    }

    @Override
    public <T> Mono<ResponseEntity<T>> post(String requestUrl, Map<String, String> headers, Object requestBody, Class<T> responseType) {
        return super.post(
                requestUrl,
                addJsonTokenToHeaders(headers),
                requestBody,
                responseType
        );
    }

    // JWT 토큰을 생성해서 Authorization 헤더로 쓸 Map에 Bearer Token으로 추가해준다
    private Map<String, String> addJsonTokenToHeaders(Map<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        if (headers.containsKey("Authorization")) {
            return headers; // 이미 Authorization 헤더가 있는 경우, 변경하지 않음
        }
        headers.put("Authorization", "Bearer " + jsonTokenManager.createToken());
        return headers;
    }
}
