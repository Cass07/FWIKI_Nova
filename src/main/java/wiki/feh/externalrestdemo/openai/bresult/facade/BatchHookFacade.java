package wiki.feh.externalrestdemo.openai.bresult.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;
import wiki.feh.externalrestdemo.util.json.IBatchResultJsonParse;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchHookFacade {

    private final BatchInfoService batchInfoService;

    @Qualifier("BatchResultJsonParseV1")
    private final IBatchResultJsonParse batchResultJsonParse;

    /**
     * webhook에서 수신한 데이터를 가공하는 facade
     */

    /**
     * batch id로 BatchInfo 조회 후, 상태가 REQUESTED인지 검증
     * @param batchId
     * @return
     */
    public Mono<BatchInfo> verifyBatchId(String batchId) {
        return batchInfoService.getBatchInfoByBatchId(batchId)
                .flatMap(batchInfo -> {
                    if(!batchInfo.getStatus().equals(BatchStatus.REQUESTED)) {
                        return Mono.error(new RuntimeException("BatchInfo status is not REQUESTED for batchId: " + batchId));
                    }
                    return Mono.just(batchInfo);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("No BatchInfo found for batchId: " + batchId)));
    }

    // batch id, jsonl String list를 받아서 map of `hero-id` & `list of BResultDto.ApiResult` 반환
    // 에러 여부에 관계없이 batchId와 jsonlList를 로깅
    public Mono<Tuple2<BatchInfo, Map<String, List<BResultDto.ApiResult>>>> processWebhookData(BatchInfo batchInfo, List<String> jsonlList) {
        return parseResponseJsonList(jsonlList)
                .map(apiResultMap -> Tuples.of(batchInfo, apiResultMap));
    }

    /**
     * jsonl string list를 받아서 hero id, ApiResultList Map으로 반환
     * @param jsonlList
     * @return
     */
    private Mono<Map<String, List<BResultDto.ApiResult>>> parseResponseJsonList(List<String> jsonlList) {
        return Flux.fromIterable(jsonlList)
                .mapNotNull(jsonString -> {
                    try {
                        Tuple2<String, String> parsed = batchResultJsonParse.parseResponseJson(jsonString);
                        if (parsed == null) {
                            log.error("Parsed JSONL String result is null: {}", jsonString);
                            return null;
                        }

                        List<BResultDto.ApiResult> apiResultList = batchResultJsonParse.parseResultStringToApiResultList(parsed.getT2());
                        if (apiResultList == null) {
                            log.error("Parsed Result List is null for heroId {}: {}", parsed.getT1(), parsed.getT2());
                            return null;
                        }

                        return Tuples.of(parsed.getT1(), apiResultList);
                    } catch (Exception e) {
                        log.error("Failed to parse JSONL string: {}", jsonString, e);
                        return null;
                    }
                })
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

}
