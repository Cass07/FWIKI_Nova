package wiki.feh.externalrestdemo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.dto.AsyncApiResponseBody;
import wiki.feh.externalrestdemo.dto.OpenAPIRequestBody;
import wiki.feh.externalrestdemo.facade.AsyncApiFacade;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AsyncApiController {
    private final AsyncApiFacade asyncApiFacade;

    @PostMapping("/api/async")
    public Mono<ResponseEntity<AsyncApiResponseBody>> startAsyncApi(@RequestBody OpenAPIRequestBody requestBody) {
        log.info("Received request to start async API with body: {}", requestBody);

        // 비동기 API 호출을 시작하고 결과를 Mono로 반환
        return asyncApiFacade.startAsyncApi(requestBody)
                .map(ResponseEntity::ok);
    }
}
