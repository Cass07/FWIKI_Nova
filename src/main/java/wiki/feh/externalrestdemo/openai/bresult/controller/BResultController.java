package wiki.feh.externalrestdemo.openai.bresult.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import wiki.feh.externalrestdemo.openai.bresult.facade.BResultFacade;
import wiki.feh.externalrestdemo.openai.bresult.facade.BatchHookFacade;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BResultController {
    private final BResultFacade bResultFacade;
    private final BatchHookFacade batchHookFacade;

    // webhook controller
    @PostMapping("/webhook/batch")
    public Mono<Void> receiveBatchResultWebhook(@RequestBody String payload) {
        // webhook 수신해서 이벤트를 분기처리해서 필요 시 하위 facade 메소드를 호출해서 처리하기
        log.info("Received batch result webhook request");
        log.info("Payload: {}", payload);

        String batchId = "batch-id-1";
        List<String> jsonList = List.of(
                """
                            {}
                        """
        );

        return webhookProcess(batchId, jsonList);
    }

    protected Mono<Void> webhookProcess(String batchId, List<String> jsonList) {
        // batchId database에서 조회하고, 작업 가능환 상태인지 검증
        return batchHookFacade.verifyBatchId(batchId)
                // 검증된 batchInfo와 jsonList를 parsing
                .flatMap(batchInfo ->
                        batchHookFacade.processWebhookData(batchInfo, jsonList)
                )
                // parsing된 데이터를 바탕으로 실제 작업 수행
                .flatMap(TupleUtils.function((batchInfo, apiResultMap) -> {
                    log.info("batchInfo status: {}", batchInfo.getStatus());
                    return bResultFacade.processInsertBResults(batchInfo, apiResultMap);
                }))
                .then()
                .doOnSuccess(_ -> log.info("Successfully processed batch result for batchId: {}", batchId))
                .onErrorResume(error -> {
                    log.error("Failed to process batch result for batchId {}: {}", batchId, error.getMessage());
                    return Mono.empty();
                });

    }
}
