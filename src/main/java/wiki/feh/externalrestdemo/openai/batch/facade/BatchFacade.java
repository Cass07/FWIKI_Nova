package wiki.feh.externalrestdemo.openai.batch.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.hero.service.HeroService;
import wiki.feh.externalrestdemo.heroquote.agg.HeroQuoteAgg;
import wiki.feh.externalrestdemo.heroquote.dto.HeroQuoteDtoConverterV1;
import wiki.feh.externalrestdemo.heroquote.service.HeroQuoteService;
import wiki.feh.externalrestdemo.openai.batch.agg.QuoteInfoAgg;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.infra.IBatchService;
import wiki.feh.externalrestdemo.openai.batch.infra.QuoteInfoAggService;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchFacade {

    private final BatchInfoService batchInfoService;
    private final QuoteInfoAggService quoteInfoAggService;
    private final HeroQuoteService heroQuoteService;
    private final HeroService heroService;

    @Qualifier("OpenAIBatchService")
    private final IBatchService openAIBatchService;

    @Qualifier("HeroQuoteDtoConverterV1")
    private final HeroQuoteDtoConverterV1 heroQuoteDtoConverterV1;

    public Mono<BatchInfo> asyncTest() {
        BatchInfo batchInfo = new BatchInfo().updateStatus(BatchStatus.PENDING);
        Mono<BatchInfo> savedBatchInfo = batchInfoService.saveBatchInfo(batchInfo);

        asyncTestInner(savedBatchInfo)
                .doOnNext(item -> log.info("Processed item: {}", item))
                .subscribeOn(Schedulers.parallel())
                .subscribe();

        return savedBatchInfo;
    }

    // 내부에서 구독하는 flux를 별도의 메소드로 구현해서 test 클래스에서 접근 가능하게 함
    public Flux<String> asyncTestInner(Mono<BatchInfo> savedBatchInfo) {
        return savedBatchInfo
                .flatMapMany(savedInfo -> {
                    int batchInfoId = savedInfo.getIdx();
                    return Flux.range(1, 3)
                            // time interval 1 sec
                            .delayElements(java.time.Duration.ofSeconds(1))
                            .map(i -> "hero-" + batchInfoId + "-" + i);
                });
    }

    /**
     * facade로 묶은 batch 요청 프로세스
     * 1. BatchInfo 객체를 생성
     * 2. BatchInfo 객체를 삽입한 후 즉시 리턴 (batchInfo 고유 idx 발급됨)
     * 3. 받은 hero Id list로부터 hero, quote list, batchQuoteInfo 튜플 플럭스 생성
     * 4. 각 튜플 객체 별로 hero, quote list로부터 AI 요청 dto 생성하고, batchQuoteInfo 객체 생성
     * 5. batchQuoteInfo 객체들을 list로 모아 batch insert
     * 6. dto list로부터 json 문자열을 생성하고, 이 데이터를 사용해서 API 요청
     * 7. API 응답을 받아서 batchInfo에 배치 고유 id와, 상태를 진행중으로 업데이트
     */

    // 외부에서 호출할 메인 로직 메소드
    public Mono<BatchInfo> requestBatchJob(List<String> heroIds) {
        BatchInfo batchInfo = new BatchInfo().updateStatus(BatchStatus.PENDING);

        return batchInfoService.saveBatchInfo(batchInfo)
                .flatMap(savedInfo ->
                        requestHeroQuoteBatchJob(heroIds, savedInfo)
                )
                .doOnError(error -> log.error("Error in batch job: {}", error.getMessage()));
    }

    // hero quote batch job 처리 메소드
    public Mono<BatchInfo> requestHeroQuoteBatchJob(List<String> heroIds, BatchInfo savedInfo) {
        int batchInfoId = savedInfo.getIdx();
        log.debug("Preparing hero quote batch info for batch id: {}", batchInfoId);

        return prepareHeroQuoteBatchInfo(heroIds, batchInfoId)
                // isQuoteInfoAggValid로 필터링하고,필터링된 객체는 로깅하기
                .filter(quoteInfoAgg -> {
                    boolean isValid = quoteInfoAgg.isValid();
                    if (!isValid) {
                        log.warn("Invalid QuoteInfoAgg for hero id {}: missing hero or quote data",
                                quoteInfoAgg.getHero() != null ? quoteInfoAgg.getHero().getId() : "null");
                    }
                    return isValid;
                })
                // batch 처리를 위해 list로 묶음
                .collectList()
                // BatchQuoteInfo 리스트 저장 먼저
                .flatMap(quoteInfoAggs -> quoteInfoAggService.saveAllBatchQuoteInfoList(quoteInfoAggs)
                        .thenReturn(quoteInfoAggs))
                // json String 리스트로 모아서 외부 API 요청
                .flatMap(quoteInfoAggs -> {
                    // quoteInfoAggs 리스트를 json body string 리스트로 변환
                    List<String> batchRequestLineJsonList = quoteInfoAggs.stream()
                            .map(heroQuoteDtoConverterV1::toJsonString)
                            .toList();
                    return openAIBatchService.callRequestBatchApi(batchRequestLineJsonList);
                })
                .flatMap(batchId -> batchInfoService.updateBatchInfoRequested(savedInfo, batchId))
                .onErrorResume(error -> {
                    log.error("Error during batch processing for batch id {}: {}", batchInfoId, error.getMessage());
                    return batchInfoService.updateBatchInfoFailed(savedInfo);
                });

    }

    /**
     * flux 플로우
     * 1. hero id list로 hero 객체들 조회
     * 2. hero id list로 hero quote list 조회
     * 3. hero id별로 묶어서 QuoteInfoAgg 객체로 매핑
     */
    private Flux<QuoteInfoAgg> prepareHeroQuoteBatchInfo(List<String> heroIds, int batchInfoId) {
        return heroQuoteService.getHeroQuoteAggByIds(heroIds)
                // Map으로 변환해서 검색 성능 개선
                .collectMap(HeroQuoteAgg::getHeroId)
                .flatMapMany(idAndQuoteAggTuple -> heroService.getHeroesByIds(heroIds)
                        .map(heroEntity -> buildQuoteInfoAgg(
                                heroEntity,
                                idAndQuoteAggTuple.getOrDefault(heroEntity.getId(), new HeroQuoteAgg()),
                                batchInfoId
                        )));

    }

    /**
     * hero 객체 flux를 생성해서 quote list에서 매핑하고 batchQuoteInfo 객체를 생성
     */
    private QuoteInfoAgg buildQuoteInfoAgg(Hero hero, HeroQuoteAgg heroQuoteAgg, int batchInfoId) {
        BatchQuoteInfo batchQuoteInfo = BatchQuoteInfo.builder()
                .batchInfoId(batchInfoId)
                .heroId(hero.getId())
                .status(BatchStatus.PENDING)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        return new QuoteInfoAgg(hero, heroQuoteAgg, batchQuoteInfo);
    }

}
