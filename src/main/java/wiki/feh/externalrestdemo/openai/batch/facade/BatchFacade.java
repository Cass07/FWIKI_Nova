package wiki.feh.externalrestdemo.openai.batch.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.hero.service.HeroService;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;
import wiki.feh.externalrestdemo.heroquote.dto.HeroQuoteDto;
import wiki.feh.externalrestdemo.heroquote.service.HeroQuoteService;
import wiki.feh.externalrestdemo.openai.api.service.OpenAPIService;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchFacade {

    private final BatchInfoService batchInfoService;
    private final BatchQuoteInfoService batchQuoteInfoService;
    private final HeroQuoteService heroQuoteService;
    private final HeroService heroService;

    private final OpenAPIService openAPIService;

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
    public Mono<BatchInfo> requestBatchJob(List<String> heroIds) {
        BatchInfo batchInfo = new BatchInfo().updateStatus(BatchStatus.PENDING);
        // BatchInfo 객체를 저장하자마자 바로 리턴하고, 이후 후처리 작업 플렉스는 saveBatchInfo가 구독된 이후 실행
        return batchInfoService.saveBatchInfo(batchInfo)
                // 후처리 플럭스 작업
                .doOnSubscribe(_ -> {
                    int batchInfoId = batchInfo.getIdx();
                    // hero, quote list, batchQuoteInfo 튜플 플럭스 생성
                    log.debug("Preparing hero quote batch info for batch id: {}", batchInfoId);
                    prepareHeroQuoteBatchInfo(heroIds, batchInfoId)
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(TupleUtils.function((hero, quotes, batchQuoteInfo) -> {
                                // quote list와 hero 객체로부터 dto 객체 생성해서 list로 묶어서 넘기고
                                return Tuples.of(
                                        hero,
                                        HeroQuoteDto.OpenAiBatchRequest.of(hero, quotes),
                                        batchQuoteInfo
                                );
                            }))
                            // quote 리스트를 json string list로 묶고 batchQuoteInfo 객체들도 묶어서 tuple2<list, list>로 리턴
                            .collect(() -> Tuples.<List<String>, List<BatchQuoteInfo>>of(new ArrayList<>(), new ArrayList<>()),
                                    (acc, tuple) -> {
                                        acc.getT1().add(HeroQuoteDto.OpenAIBatchText.of(tuple.getT2()).getText());
                                        acc.getT2().add(tuple.getT3());
                                    })
                            // batchQuoteInfo 리스트를 batch insert
                            .flatMap(TupleUtils.function((batchRequestLineList, batchQuoteInfoList) ->
                                    batchQuoteInfoService.saveAllBatchQuoteInfoList(batchQuoteInfoList)
                                            .thenReturn(batchRequestLineList)
                            ))
                            // json string 리스트로 외부 api 요청
                            .flatMap(openAPIService::callRequestBatchApi)
                            // 응답을 받아서 batchInfo 업데이트
                            .flatMap(batchId -> updateBatchInfoRunning(batchInfo, batchId))
                            .switchIfEmpty(Mono.error(() -> new IllegalStateException("Batch processing failed")))
                            // 에러 처리
                            .doOnError(error -> {
                                log.error("Error during batch processing for batch id {}: {}", batchInfoId, error.getMessage());
                                updateBatchInfoFailed(batchInfo)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .subscribe();
                            })
                            .subscribe();
                });

    }

    private Mono<BatchInfo> updateBatchInfoFailed(BatchInfo batchInfo) {
        batchInfo.updateStatus(BatchStatus.FAILED);
        return batchInfoService.saveBatchInfo(batchInfo);
    }

    // BatchInfo의 batchId를 갱신하고 상태를 RUNNING으로 업데이트
    private Mono<BatchInfo> updateBatchInfoRunning(BatchInfo batchInfo, String batchId) {
        batchInfo.updateBatchId(batchId);
        batchInfo.updateStatus(BatchStatus.RUNNING);
        return batchInfoService.saveBatchInfo(batchInfo);
    }

    /**
     * flux 플로우
     * 1. hero id list로 hero 객체들 조회
     * 2. hero id list로 hero quote list 조회
     * 3. hero id별로 묶어서 hero, quote list, batchQuoteInfo 객체 생성
     */
    private Flux<Tuple3<Hero, List<HeroQuote>, BatchQuoteInfo>> prepareHeroQuoteBatchInfo(List<String> heroIds, int batchInfoId) {
        return heroQuoteService.getQuotesAndIdByIds(heroIds)
                // 성능 개선을 위해 튜플을 Map으로 변환하고
                .collectMap(Tuple2::getT1, Tuple2::getT2)
                // 각 hero 객체별로 튜플 매핑해서 리턴
                .flatMapMany(idAndQuoteListTuple -> heroService.getHeroesByIds(heroIds)
                        .map(heroEntity -> buildHeroQuoteBatchInfoTuple(
                                heroEntity,
                                idAndQuoteListTuple.getOrDefault(heroEntity.getId(), List.of()),
                                batchInfoId
                        )));
    }

    /**
     * hero 객체 flux를 생성해서 quote list에서 매핑하고 batchQuoteInfo 객체를 생성해서 튜플로 묶기
     */
    private Tuple3<Hero, List<HeroQuote>, BatchQuoteInfo> buildHeroQuoteBatchInfoTuple(Hero hero, List<HeroQuote> quotes, int batchInfoId) {
        BatchQuoteInfo batchQuoteInfo = BatchQuoteInfo.builder()
                .batchInfoId(batchInfoId)
                .heroId(hero.getId())
                .status(BatchStatus.PENDING)
                .build();
        return Tuples.of(hero, quotes, batchQuoteInfo);
    }

}
