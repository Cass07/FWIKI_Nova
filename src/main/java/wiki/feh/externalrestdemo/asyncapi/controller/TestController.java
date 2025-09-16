package wiki.feh.externalrestdemo.asyncapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.asyncapi.service.testapireq.TestApiReqService;

@RestController
@AllArgsConstructor
public class TestController {
    private final ObjectMapper objectMapper;

    private final TestApiReqService testApiReqService;

    @GetMapping("/test")
    public Mono<ResponseEntity<Object>> testApi() {

        return testApiReqService.testApiGet()
                .mapNotNull(response -> {
                    System.out.println(response);
                    return ResponseEntity.ok(
                            objectMapper.convertValue(response.getBody(), Object.class)
                    );
                });
    }

    @GetMapping("/test/several")
    public Mono<ResponseEntity<Object>> testApiSeveral() {
        return testApiReqService.testApiGetSeveral()
                .mapNotNull(response -> {
                    System.out.println(response);
                    return ResponseEntity.ok(
                            objectMapper.convertValue(response, Object.class)
                    );
                });
    }


}
