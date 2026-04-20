package wiki.feh.externalrestdemo.openai.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.facade.BatchFacade;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BatchController {
    private final BatchFacade batchFacade;

    public Mono<BatchInfo> requestBatchJobListener(List<String> heroIds) {
        log.info("Received batch create request from SQS listener");

        return batchFacade.requestBatchJobListener(heroIds)
                .doOnNext(batchInfo -> log.info("Batch job requested with id: {}", batchInfo.getIdx()));
    }
}
