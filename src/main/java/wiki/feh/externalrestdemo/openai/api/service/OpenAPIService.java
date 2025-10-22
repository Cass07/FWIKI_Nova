package wiki.feh.externalrestdemo.openai.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API를 제외한 나머지 로직 개발을 위해서 우선 모킹 서비스를 구현
 */
@Slf4j
@Component
public class OpenAPIService {

    public Mono<String> callRequestBatchApi(List<String> jsonlList) {
        // 실제 OpenAI API 호출 로직이 들어가야 함
        return Mono.just(LocalDateTime.now().toString())
                .doOnNext(res -> log.info("Batch API called with result: {}", res))
                .delayElement(java.time.Duration.ofSeconds(1)); // 1초 지연 추가
    }
}
