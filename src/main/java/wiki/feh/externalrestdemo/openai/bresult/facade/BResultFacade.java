package wiki.feh.externalrestdemo.openai.bresult.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.heroquotekr.domain.HeroQuoteKr;
import wiki.feh.externalrestdemo.heroquotekr.service.HeroQuoteKrService;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;
import wiki.feh.externalrestdemo.util.NamedLockManager;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BResultFacade {
    private final String HERO_QUOTE_KR_LOCK_PREFIX = "hquote_";
    private final HeroQuoteKrService heroQuoteKrService;
    private final BatchQuoteInfoService batchQuoteInfoService;
    private final NamedLockManager namedLockManager;
    private final BatchInfoService batchInfoService;

    /*
     * flow
     *
     * 0>
     * 1. batch result jsonl 수신함
     * 2. batch result batch id를 읽어서 batchInfo 조회함 - batchid는 jsonl에 없고 데이터 다운로드 시 이미 가지고 있는걸로 써야함
     * 3. running인지 확인 (아니면 종료)
     * 4. findAll로 batchInfo id에 해당하는 BatchQuoteInfo 리스트 조회
     * 5. jsonl 읽어서 필요한 데이터만 가공하면서 batchQuoteInfo 리스트와 매핑하기
     *
     * flux에 넘겨줘야 하는거 : BatchQuoteInfo, list of BResultDto.ApiResult
     *
     * 0-1. BatchQuoteInfo에 heroId로 BatchStatus.PENDING 상태인게 있는지 확인 (없으면 이하 작업은 하지 않고 종료) (상위에서 걸러서 넘겨줄것)
     * 1. jsonl을 읽어서 heroid와 BResultDto.ListRequest로 변환한 것을 받아온다 (receiveResultMock)
     * 2. heroId lock 획득
     * 3. heroId로 기존 heroQuoteKr 조회해서, 신규 인덱스 생성 (데이터 없으면 1, 아니면 ++)
     * 4. BResultDto.ListRequest를 HeroQuoteKr 엔티티로 변환
     * 4-1. 신규 데이터가 요청 생성 후에 들어왔는지 확인해서 공개 여부 설정 구현 (일단은 무조건 공개로)
     * 5. heroQuoteKr Batch 저장
     * 6. lock 해제
     * 7. BatchQuoteInfo 상태 COMPLETED로 변경
     *
     * - 상위 블록애서 BatchInfo의 상태 변경, logging 필요
     */

    /**
     * BatchInfo 객체와 Dto List를 받아서 (BatchInfo는 존재함이 보장됨)
     * 작업 시작 전 batchInfo 상태가 requested인지 확인하고, running으로 바꿈
     * 그 후, 각 BatchQuoteInfo에 대해 processAndInsertHeroQuoteKr 실행
     * 모두 종료되면 batchInfo 상태를 completed로 바꿈
     */
    public Mono<BatchInfo> insertApiResultToHeroQuoteKr(BatchInfo batchInfo, Map<String, List<BResultDto.ApiResult>> resultList) {
        return Mono.just(batchInfo)
                .flatMap(bi -> {
                    if (!bi.getStatus().equals(BatchStatus.REQUESTED)) {
                        log.warn("BatchInfo id {} is not in Requested status. Current status: {}. Aborting process.", bi.getIdx(), bi.getStatus());
                        return Mono.empty();
                    }
                    return batchInfoService.updateBatchInfoRunning(bi);
                })
                // 내부 작업이 모두 끝날 때까지 원본 객체를 유지하며 작업 완료 대기
                .delayUntil(bi ->
                        // 각 batchQuoteInfo에 대해서 맞는 resultList를 가지고 processAndInsertHeroQuoteKr 실행
                        batchQuoteInfoService.findBatchQuoteInfoByBatchInfoId(bi.getIdx())
                                // batchQuoteInfo를 순회하면서 resultList에서 매칭되는 heroId가 있는지 확인하고, 있으면 lockHeroIdAndInsertHeroQuoteKr 실행
                                .flatMap(batchQuoteInfo -> {
                                    if (!resultList.containsKey(batchQuoteInfo.getHeroId())) {
                                        log.warn("No results found for heroId {} in BatchQuoteInfo id {}. Skipping.",
                                                batchQuoteInfo.getHeroId(), batchQuoteInfo.getIdx());
                                        return Mono.empty();
                                    }
                                    return lockHeroIdAndInsertHeroQuoteKr(batchQuoteInfo, resultList.get(batchQuoteInfo.getHeroId()));
                                })
                                .then(Mono.defer(() -> {
                                    // bi 이하의 batchQuoteInfo 중 Pending인 것을 FAILED로 변경
                                    log.info("All BatchQuoteInfo processing completed for BatchInfo id {}", bi.getIdx());
                                    return batchQuoteInfoService.updateBatchQuoteInfoListStatusToFailed(bi.getIdx());
                                }))  // 모든 작업 완료 대기
                )
                .flatMap(batchInfoService::updateBatchInfoCompleted);
    }


    /**
     * heroId로 named lock을 획득한 후, insertHeroQuoteKr 실행
     *
     * @param batchQuoteInfo batch quote info entity
     * @param results        apiResult list (OpenAI response를 parsing한 것)
     * @return Mono<BatchQuoteInfo>
     */
    public Mono<BatchQuoteInfo> lockHeroIdAndInsertHeroQuoteKr(BatchQuoteInfo batchQuoteInfo, List<BResultDto.ApiResult> results) {
        String lockKey = HERO_QUOTE_KR_LOCK_PREFIX + batchQuoteInfo.getHeroId();
        return namedLockManager.executeWithNamedLock(lockKey,
                        () -> insertHeroQuoteKr(batchQuoteInfo, results)
                )
                .then(Mono.defer(() -> batchQuoteInfoService.updateBatchQuoteInfoComplete(batchQuoteInfo)));
    }

    /**
     * heroId로 기존 kr 데이터를 조회해서, 신규 인덱스를 생성하여 entity list를 생성하고 이를 batch save
     *
     * @param batchQuoteInfo batch quote info entity
     * @param results        apiResult list (OpenAI response를 parsing한 것)
     * @return void
     */
    public Mono<Void> insertHeroQuoteKr(BatchQuoteInfo batchQuoteInfo, List<BResultDto.ApiResult> results) {
        String heroId = batchQuoteInfo.getHeroId();
        return heroQuoteKrService.getLatestIndexForHeroQuoteKr(heroId)
                .flatMap(index -> {
                            // 기존 데이터가 없을때만 공개 데이터로 설정, 아니면 비공개로 설정
                            // TODO:: 신장만 갱신할때 생각 필요
                            int status = 1;
                            if (index != 0) {
                                status = 2;
                            }
                            return convertResultToHeroQuoteKr(heroId, index + 1, status, results);
                        }
                )
                .flatMap(heroQuoteKrService::batchSaveHeroQuoteKrList);
    }

    /**
     * API 결과를 HeroQuoteKr 엔티티 리스트로 변환
     *
     * @param heroId   hero idx
     * @param newIndex 신규 version index
     * @param status   공개 여부 (1: 공개, 2: 비공개)
     * @param results  apiResult list (OpenAI response를 parsing한 것)
     * @return HeroQuoteKr list Mono
     */
    private Mono<List<HeroQuoteKr>> convertResultToHeroQuoteKr(String heroId, int newIndex, int status, List<BResultDto.ApiResult> results) {
        return Flux.fromIterable(results)
                .map(apiResult -> new HeroQuoteKr(
                        apiResult,
                        heroId,
                        newIndex,
                        status
                ))
                .collectList();
    }


}
