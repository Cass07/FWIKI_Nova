package wiki.feh.externalrestdemo.openai.bresult.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
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
        return batchHookFacade.verifyBatchId(batchId)
                .flatMap(batchInfo -> {
                    log.info("Verified batchInfo: {}", batchInfo.getBatchId());
                    // batchId로 결과 파일 ID 조회
                    String bId = batchInfo.getBatchId();
                    return batchResultService.getBatchResultFileId(bId)
                            .switchIfEmpty(Mono.error(new RuntimeException("No result file found for batchId: " + bId)))
                            // 결과 파일 ID로 파일 내용 조회
                            .flatMap(fileId -> {
                                log.info("Retrieved output file ID: {}", fileId);
                                return batchResultService.getFileContentById(fileId);
                            })
                            // 파일 내용을 줄 단위로 분리해서 list로 묶음
                            .flatMapMany(fileContent -> Flux.fromArray(fileContent.split("\n")))
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
                            .doOnSuccess(_ -> log.info("Successfully processed batch result for batchId: {}", bId))
                            .doOnError(error -> {
                                log.error("Failed to process batch result for batchId {}: {}", bId, error.getMessage());
                                //return Mono.empty();
                            });
                });
    }
}
