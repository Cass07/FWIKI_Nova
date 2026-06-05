package wiki.feh.externalrestdemo.openai.bresult.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.bresult.facade.BResultFacade;
import wiki.feh.externalrestdemo.openai.bresult.infra.IBatchResultService;

@Slf4j
@RequiredArgsConstructor
@Controller
public class BResultController {
    private final BResultFacade bResultFacade;

    @Qualifier("OpenAIBatchResultService")
    private final IBatchResultService batchResultService;

    /**
     * batchId로부터 Batch Result 를 조회해서 DB에 저장
     * @param batchId
     * @return
     */
    public Mono<Void> updateTranslateData(String batchId) {
        // batchId database에서 조회하고, 작업 가능한 상태인지 검증
        return bResultFacade.updateTranslateDataFromBatchId(batchId)
                .doOnSuccess(_ -> log.info("Batch result updated for batchId: {}", batchId))
                .doOnError(e -> log.error("Failed to update batch result for batchId: {}", batchId, e))
                .then();
    }

    /**
     * batchId 의 결과가 유효하지 않거나 실패한 경우, batchId를 실패로 업데이트
     * @param batchId
     * @return
     */
    public Mono<Void> updateBatchAsFailed(String batchId) {
        return bResultFacade.updateBatchAsFailed(batchId)
                .doOnSuccess(_ -> log.info("Batch marked as failed for batchId: {}", batchId))
                .doOnError(e -> log.error("Failed to mark batch as failed for batchId: {}", batchId, e))
                .then();
    }
}
