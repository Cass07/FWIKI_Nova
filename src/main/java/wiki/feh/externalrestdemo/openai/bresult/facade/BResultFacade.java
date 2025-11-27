package wiki.feh.externalrestdemo.openai.bresult.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteKr;
import wiki.feh.externalrestdemo.heroquote.service.HeroQuoteKrService;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;
import wiki.feh.externalrestdemo.util.NamedLockManager;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BResultFacade {
    private final String HERO_QUOTE_KR_LOCK_PREFIX = "hero_quote_";
    private final String HERO_QUOTE_KR_KIND = "Home";
    private final int HERO_QUOTE_KR_SEQ = 1;
    private final HeroQuoteKrService heroQuoteKrService;
    private final NamedLockManager namedLockManager;

    /*
     * flow
     * 1. jsonl을 읽어서 heroid와 BResultDto.ListRequest로 변환한 것을 받아온다 (receiveResultMock)
     * 2. heroId lock 획득
     * 3. heroId로 기존 heroQuoteKr 조회해서, 신규 인덱스 생성 (데이터 없으면 1, 아니면 ++)
     * 4. BResultDto.ListRequest를 HeroQuoteKr 엔티티로 변환
     * 4-1. 신규 데이터가 요청 생성 후에 들어왔는지 확인해서 공개 여부 설정 구현 (일단은 무조건 공개로)
     * 5. heroQuoteKr Batch 저장
     * 6. lock 해제
     */

    /**
     * heroId로 named lock을 획득한 후, insertHeroQuoteKr 실행
     * @param heroId hero idx
     * @param results apiResult list (OpenAI response를 parsing한 것)
     * @return
     */
    public Mono<Void> processAndInsertHeroQuoteKr(String heroId, List<BResultDto.ApiResult> results) {
        String lockKey = HERO_QUOTE_KR_LOCK_PREFIX + heroId;
        return namedLockManager.executeWithNamedLock(lockKey,
                insertHeroQuoteKr(heroId, results)
        );
    }

    /**
     * heroId로 기존 kr 데이터를 조회해서, 신규 인덱스를 생성하여 entity list를 생성하고 이를 batch save
     * @param heroId hero idx
     * @param results apiResult list (OpenAI response를 parsing한 것)
     * @return void
     */
    public Mono<Void> insertHeroQuoteKr(String heroId, List<BResultDto.ApiResult> results) {
        return getLatestHeroQuoteKrIndex(heroId)
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

    /**
     * heroId로 기존 heroQuoteKr 데이터에서 최신 인덱스 조회
     * @param heroId hero idx
     * @return latest index Mono
     */
    private Mono<Integer> getLatestHeroQuoteKrIndex(String heroId) {
        return heroQuoteKrService.findLatestVersionByIdKindSeq(heroId, HERO_QUOTE_KR_KIND, HERO_QUOTE_KR_SEQ);
    }


}
