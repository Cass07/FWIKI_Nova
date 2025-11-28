package wiki.feh.externalrestdemo.openai.bresult.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteKr;
import wiki.feh.externalrestdemo.heroquote.service.HeroQuoteKrService;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;
import wiki.feh.externalrestdemo.util.NamedLockManager;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BResultFacade {
    private final String HERO_QUOTE_KR_LOCK_PREFIX = "hquote_";
    private final HeroQuoteKrService heroQuoteKrService;
    private final BatchQuoteInfoService batchQuoteInfoService;
    private final NamedLockManager namedLockManager;

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
     *
     * data가 있는데 BatchQuoteInfo가 없으면 버릴 것인가???? <-버려야지...
     */

    /**
     * heroId로 named lock을 획득한 후, insertHeroQuoteKr 실행
     * @param batchQuoteInfo batch quote info entity
     * @param results apiResult list (OpenAI response를 parsing한 것)
     * @return void mono
     */
    public Mono<Void> processAndInsertHeroQuoteKr(BatchQuoteInfo batchQuoteInfo, List<BResultDto.ApiResult> results) {
        String lockKey = HERO_QUOTE_KR_LOCK_PREFIX + batchQuoteInfo.getHeroId();
        return namedLockManager.executeWithNamedLock(lockKey,
                insertHeroQuoteKr(batchQuoteInfo, results)
        )
        .then(batchQuoteInfoService.updateBatchQuoteInfoComplete(batchQuoteInfo))
        .then();
    }

    /**
     * heroId로 기존 kr 데이터를 조회해서, 신규 인덱스를 생성하여 entity list를 생성하고 이를 batch save
     * @param batchQuoteInfo batch quote info entity
     * @param results apiResult list (OpenAI response를 parsing한 것)
     * @return void
     */
    public Mono<Void> insertHeroQuoteKr(BatchQuoteInfo batchQuoteInfo, List<BResultDto.ApiResult> results) {
        String heroId = batchQuoteInfo.getHeroId();
        return heroQuoteKrService.getLatestIndexForHeroQuoteKr(heroId)
                .flatMap(index ->
                        convertResultToHeroQuoteKr(heroId, index + 1, 1, results)
                )
                .flatMap(heroQuoteKrService::batchSaveHeroQuoteKrList);
    }

    /**
     * API 결과를 HeroQuoteKr 엔티티 리스트로 변환
     * @param heroId hero idx
     * @param newIndex 신규 version index
     * @param status 공개 여부 (1: 공개, 2: 비공개)
     * @param results apiResult list (OpenAI response를 parsing한 것)
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
