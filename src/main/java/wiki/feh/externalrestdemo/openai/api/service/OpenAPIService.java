package wiki.feh.externalrestdemo.openai.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API를 제외한 나머지 로직 개발을 위해서 우선 모킹 서비스를 구현
 * API Result json을 가공해서 넘겨주는 책임은 여기서 지도록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAPIService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<String> callRequestBatchApi(List<String> jsonlList) {
        // 실제 OpenAI API 호출 로직이 들어가야 함
        return Mono.just(LocalDateTime.now().toString())
                .doOnNext(res -> {
                    log.info("Mock OpenAI Batch API called successfully.");
                    for (String jsonString : jsonlList) {
                        log.debug("Payload item: {}", jsonString);
                    }
                })
                .delayElement(java.time.Duration.ofSeconds(1)); // 테스트용 1초 지연 추가
    }

    // hero id, json result string을 받아서 (본문 전체말고 결과 스트링만), hero id/apiResult list tuple list로 반환할것
    public Flux<Tuple2<String, List<BResultDto.ApiResult>>> receiveResultMock(List<Tuple2<String, String>> responseBody) {
        return Flux.fromIterable(responseBody)
                .map(TupleUtils.function((heroId, jsonResult) -> {
                    log.debug("Processing result for heroId: {}", heroId);
                    log.debug("JSON Result: {}", jsonResult);
                    // json string을 파싱해서 ApiResult 리스트로 변환.
                    try {
                        List<BResultDto.ApiResult> apiResults = objectMapper.readValue(
                                jsonResult, new TypeReference<>() {});
                        return Tuples.of(heroId, apiResults);
                    } catch (Exception e) {
                        // parsing에 실패하면 일단 빈 리스트를 반환하기
                        log.error("Error parsing JSON result for heroId {}: {}", heroId, e.getMessage());
                        return Tuples.of(heroId, java.util.Collections.<BResultDto.ApiResult>emptyList());
                    }
                }))
                .doOnNext(res -> log.info("Mock OpenAI API result processed."));
    }
}
