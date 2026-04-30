package wiki.feh.externalrestdemo.openai.bresult.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
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

    public Mono<Void> updateTranslateData(String batchId) {
        // batchId database에서 조회하고, 작업 가능한 상태인지 검증
        return bResultFacade.updateTranslateDataFromBatchId(batchId)
                .doOnSuccess(_ -> log.info("Batch result updated for batchId: {}", batchId))
                .doOnError(e -> log.error("Failed to update batch result for batchId: {}", batchId, e))
                .then();
    }
}
