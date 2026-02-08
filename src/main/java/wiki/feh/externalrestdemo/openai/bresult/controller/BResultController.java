package wiki.feh.externalrestdemo.openai.bresult.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import wiki.feh.externalrestdemo.openai.api.service.OpenAPIService;
import wiki.feh.externalrestdemo.openai.bresult.facade.BResultFacade;
import wiki.feh.externalrestdemo.openai.bresult.facade.BatchHookFacade;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BResultController {
    private final BResultFacade bResultFacade;
    private final BatchHookFacade batchHookFacade;
    private final OpenAPIService openAPIService;

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

    @GetMapping("/api/v1/webhook/result/{batchId}")
    public Mono<Void> testWebhook(@PathVariable String batchId) {

        // batchId database에서 조회하고, 작업 가능한 상태인지 검증
        return batchHookFacade.verifyBatchId(batchId)
                .flatMap(batchInfo -> {
                    log.info("Verified batchInfo: {}", batchInfo.getBatchId());
                    // batchId로 결과 파일 ID 조회
                    String verifyBatchId = batchInfo.getBatchId();
                    return openAPIService.getBatchResultFileId(verifyBatchId)
                            .switchIfEmpty(Mono.error(new RuntimeException("No result file found for batchId: " + verifyBatchId)))
                            // 결과 파일 ID로 파일 내용 조회
                            .flatMap(fileId -> {
                                log.info("Retrieved output file ID: {}", fileId);
                                return openAPIService.getFileContentById(fileId);
                            })
                            // 파일 내용을 줄 단위로 분리해서 list로 묶음
                            .flatMapMany(fileContent -> {
                                //log.debug("Retrieved file content for batchId {}: {}", verifyBatchId, fileContent);
                                return Flux.fromArray(fileContent.split("\n"));
                            })
                            .collectList()
                            // 검증된 batchInfo와 jsonList를 parsing
                            .flatMap(jsonList ->
                                    batchHookFacade.processWebhookData(batchInfo, jsonList)
                            )
                            // parsing된 데이터를 바탕으로 실제 작업 수행
                            .flatMap(TupleUtils.function((batchInfo_, apiResultMap) -> {
                                log.info("batchInfo status: {}", batchInfo_.getStatus());
                                return bResultFacade.processInsertBResults(batchInfo_, apiResultMap);
                            }))
                            .then()
                            .doOnSuccess(_ -> log.info("Successfully processed batch result for batchId: {}", verifyBatchId))
                            .doOnError(error -> {
                                log.error("Failed to process batch result for batchId {}: {}", verifyBatchId, error.getMessage());
                                //return Mono.empty();
                            });
                });
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
                .doOnError(error -> {
                    log.error("Failed to process batch result for batchId {}: {}", batchId, error.getMessage());
                    throw new RuntimeException(error);
                    //return Mono.empty();
                });

    }
}
