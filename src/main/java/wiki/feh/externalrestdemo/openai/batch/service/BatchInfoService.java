package wiki.feh.externalrestdemo.openai.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;
import wiki.feh.externalrestdemo.heroquote.dto.HeroQuoteDto;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfoRepository;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.util.JsonStringUtil;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BatchInfoService {
    private final BatchInfoRepository batchInfoRepository;

    public Mono<BatchInfo> saveBatchInfo(BatchInfo batchInfo) {
        return batchInfoRepository.save(batchInfo)
                .doOnSuccess(saved -> {
                    // 저장 성공 시 로그 출력
                    log.info("BatchInfo saved with id: {}" , saved.getIdx());
                })
                .doOnError(error -> {
                    // 저장 실패 시 로그 출력
                    log.error("Error saving BatchInfo: {}" , error.getMessage());
                    throw new RuntimeException("Error saving BatchInfo", error);
                });
    }

}
