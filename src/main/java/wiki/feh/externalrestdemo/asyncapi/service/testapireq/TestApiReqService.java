package wiki.feh.externalrestdemo.asyncapi.service.testapireq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import wiki.feh.externalrestdemo.asyncapi.service.webclient.TestAPIClient;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestApiReqService {
    private final TestAPIClient testAPIClient;
    private final ObjectMapper objectMapper;

    // 작업 결과를 저장하진 않을 거라 db 접근이 필요 없으므로 repository는 없음

    // WebClient Get API 호출 예시
    public Mono<ResponseEntity<String>> testApiGet() {
        return testAPIClient.get("", null, null, String.class)
                .mapNotNull(response -> {
                    log.info("Response from test API: {}", response);
                    return response;
                });
    }

    public Mono<List<String>> testApiGetSeveral() {
        // 요청 10회용 flux 생성
        Flux<Integer> requests = Flux.just(1, 2, 3);

        // parallel 처리로 동시에 호출
        return requests.parallel()
                .runOn(Schedulers.parallel())
                // 각 요청을 requestBody로 변환
                .map(request -> Map.of(
                        "requestId" , request,
                        "responseBody", "{\"request\" : " + request + "}",
                        "delay", "5000"
                ))
                .flatMap(requestBody -> testAPIClient.post("", null, requestBody, String.class)
                        .mapNotNull(response -> {
                            log.info("Response for request {}: {}", requestBody.get("requestId"), response);
                            return response.getBody();
                        }))
                // parallel 처리된 결과를 순차적으로 모아서 리스트로 반환
                .sequential()
                .collectList();
    }

}
