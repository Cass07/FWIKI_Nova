package wiki.feh.externalrestdemo.openai.bresult.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import wiki.feh.externalrestdemo.openai.bresult.facade.BResultFacade;
import wiki.feh.externalrestdemo.openai.bresult.facade.BatchHookFacade;
import wiki.feh.externalrestdemo.openai.bresult.infra.IBatchResultService;

@Slf4j
@RequiredArgsConstructor
@Controller
public class BResultController {
    private final BResultFacade bResultFacade;
    private final BatchHookFacade batchHookFacade;

    @Qualifier("OpenAIBatchResultService")
    private final IBatchResultService batchResultService;

    public Mono<Void> updateTranslateData(String batchId) {
        // batchId database에서 조회하고, 작업 가능한 상태인지 검증
        return batchHookFacade.getUpdatableBatchInfoFromBatchId(batchId)
                .flatMap(batchInfo -> {
                    log.info("Verified batchInfo: {}", batchInfo.getBatchId());
                    // batchId로 결과 파일 ID 조회
                    String bId = batchInfo.getBatchId();
                    return batchResultService.getJsonListFromBatchId(bId)
                            // 검증된 batchInfo와 jsonList를 parsing
                            // parsing은 기존에 정의한 jsonl 구조에 따라 가기 때문에 API와는 독립적으로 설계
                            .flatMap(jsonList ->
                                    batchHookFacade.processWebhookData(batchInfo, jsonList)
                            )
                            // parsing된 데이터를 바탕으로 실제 작업 수행
                            .flatMap(TupleUtils.function((batchInfo_, apiResultMap) -> {
                                log.info("batchInfo status: {}", batchInfo_.getStatus());
                                return bResultFacade.insertApiResultToHeroQuoteKr(batchInfo_, apiResultMap);
                            }))
                            .then()
                            .doOnSuccess(_ -> log.info("Successfully processed batch result for batchId: {}", bId))
                            .doOnError(error -> {
                                log.error("Failed to process batch result for batchId {}: {}", bId, error.getMessage());
                                //return Mono.empty();
                            });
                });
    }
}
