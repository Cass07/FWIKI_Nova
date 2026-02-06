package wiki.feh.externalrestdemo.openai.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.facade.BatchFacade;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BatchController {
    private final BatchFacade batchFacade;

    // hero id list를 받아서 batch create하고 반환된 batch Idx를 리턴
    @PostMapping("/api/v1/transelate/batch")
    public Mono<String> requestBatchCreate(@RequestBody List<String> heroIdList) {
        log.info("Received batch create request");

        return batchFacade.requestBatchJob(heroIdList)
                .doOnNext(batchInfo -> log.info("Batch job requested with id: {}", batchInfo.getIdx()))
                .map(batchInfo -> Integer.toString(batchInfo.getIdx()));
    }
}
