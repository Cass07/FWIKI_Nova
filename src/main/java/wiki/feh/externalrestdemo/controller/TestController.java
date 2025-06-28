package wiki.feh.externalrestdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.service.webclient.TestAPIClient;

@RestController
@AllArgsConstructor
public class TestController {
    private final TestAPIClient testAPIClient;
    private final ObjectMapper objectMapper;

    @GetMapping("/test")
    public Mono<ResponseEntity<Object>> testApi() {
        return testAPIClient.get("", null, null, String.class)
                .mapNotNull(response -> {
                    System.out.println(response);
                    return ResponseEntity.ok(
                            objectMapper.convertValue(response.getBody(), Object.class)
                    );
                });
    }
}
