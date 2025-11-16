package wiki.feh.externalrestdemo.openai.batch.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.hero.service.HeroService;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;
import wiki.feh.externalrestdemo.heroquote.domain.QuoteLang;
import wiki.feh.externalrestdemo.heroquote.service.HeroQuoteService;
import wiki.feh.externalrestdemo.openai.api.service.OpenAPIService;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BatchFacadeTest {
    @InjectMocks
    private BatchFacade batchFacade;

    @Mock
    private BatchInfoService batchInfoService;

    @Mock
    private BatchQuoteInfoService batchQuoteInfoService;

    @Mock
    private HeroQuoteService heroQuoteService;

    @Mock
    private HeroService heroService;

    @Mock
    private OpenAPIService openAPIService;

    @DisplayName("asyncTest 테스트")
    @Test
    public void testAsyncTest() {
        // given
        BatchInfo batchInfo = new BatchInfo(1, null, BatchStatus.PENDING, LocalDateTime.now(), null);
        doReturn(Mono.just(batchInfo))
                .when(batchInfoService).saveBatchInfo(any(BatchInfo.class));

        // when & then
        StepVerifier
                .create(batchFacade.asyncTest())
                .expectSubscription()
                .as("Async Test")
                .expectNextMatches(result -> result.getIdx() == 1)
                .verifyComplete();
    }

    @DisplayName("asyncTestInner 테스트")
    @Test
    public void testAsyncTestInner() {
        // given
        BatchInfo batchInfo = new BatchInfo(1, null, BatchStatus.PENDING, LocalDateTime.now(), null);
        Mono<BatchInfo> savedBatchInfoMono = Mono.just(batchInfo);

        // when
        Flux<String> resultFlux = batchFacade.asyncTestInner(savedBatchInfoMono);

        // then
        StepVerifier
                .create(resultFlux)
                .expectSubscription()
                .as("Async Test Inner")
                .expectNext("hero-1-1")
                .expectNext("hero-1-2")
                .expectNext("hero-1-3")
                .verifyComplete();
    }

    @DisplayName("asyncTestInner 테스트 - emit 순서가 보장되지 않을 떄")
    @Test
    public void testAsyncTestInner_Unordered() {
        // given
        BatchInfo batchInfo = new BatchInfo(1, null, BatchStatus.PENDING, LocalDateTime.now(), null);
        Mono<BatchInfo> savedBatchInfoMono = Mono.just(batchInfo);

        // when
        Flux<String> resultFlux = batchFacade.asyncTestInner(savedBatchInfoMono);

        // then
        StepVerifier
                .create(resultFlux)
                .expectSubscription()
                .as("Async Test Inner Unordered")
                .recordWith(java.util.ArrayList::new)
                .expectNextCount(3)
                .consumeRecordedWith(results -> {
                    assertTrue(results.contains("hero-1-3"));
                    assertTrue(results.contains("hero-1-2"));
                    assertTrue(results.contains("hero-1-1"));
                })
                .verifyComplete();
    }

    @DisplayName("requestBatchJob 테스트")
    @Test
    public void testRequestBatchJob() {
        // given
        BatchInfo batchInfo = new BatchInfo(1, null, BatchStatus.PENDING, LocalDateTime.now(), null);

        doReturn(Mono.just(batchInfo))
                .when(batchInfoService).saveBatchInfo(any(BatchInfo.class));

        // when & then
        StepVerifier
                .create(batchFacade.requestBatchJob(List.of()))
                .expectSubscription()
                .as("Request Batch Job Test")
                .expectNextMatches(result -> result.getIdx() == 1)
                .verifyComplete();
    }


    @DisplayName("HeroQuoteBatchJob 테스트")
    @Test
    public void testHeroQuoteBatchJob_endToEnd_captor() {
        // given
        String hero1 = "hero1";
        String hero2 = "hero2";
        List<String> heroIds = List.of(hero1, hero2);
        Hero hero1Hero = new Hero(hero1, "Hero1Kor", "HeroOneKorSub", "Hero1Jp", "HeroOneJpSub","Hero1", LocalDate.now());
        Hero hero2Hero = new Hero(hero2, "Hero2Kor", "HeroTwoKorSub", "Hero2Jp", "HeroTwoJpSub","Hero2", LocalDate.now());
        LocalDateTime now = LocalDateTime.now();

        List<Hero> heroList = List.of(hero1Hero, hero2Hero);

        HeroQuote heroQuote1_1 = new HeroQuote(1, hero1, "Kind", 1, "quote1", QuoteLang.JP);
        HeroQuote heroQuote1_2 = new HeroQuote(2, hero1, "Brave", 1, "quote2", QuoteLang.JP);
        HeroQuote heroQuote2_1 = new HeroQuote(3, hero2, "Kind", 1, "quote3", QuoteLang.JP);
        HeroQuote heroQuote2_2 = new HeroQuote(4, hero2, "Brave", 1, "quote4", QuoteLang.JP);

        BatchInfo batchInfo = new BatchInfo(1, null, BatchStatus.PENDING, now, null);

        ArgumentCaptor<List<BatchQuoteInfo>> captor = ArgumentCaptor.forClass(List.class);

        doReturn(Flux.fromIterable(heroList))
                .when(heroService).getHeroesByIds(heroIds);

        doReturn(Flux.just(
                Tuples.of(heroQuote1_1, heroQuote1_2),
                Tuples.of(heroQuote2_1, heroQuote2_2)
        )).when(heroQuoteService).getQuotesAndIdByIds(heroIds);

        doReturn(Mono.empty())
                .when(batchQuoteInfoService).saveAllBatchQuoteInfoList(anyList());


        String batchApiResponseId = "external-batch-id-123";

        doReturn(Mono.just(batchApiResponseId))
                .when(openAPIService).callRequestBatchApi(any());

        doReturn(Mono.just(batchInfo.updateBatchId("external-batch-id-123").updateStatus(BatchStatus.RUNNING)))
                .when(batchInfoService).updateBatchInfoRunning(any(BatchInfo.class), eq(batchApiResponseId));

        // when, then
        StepVerifier.create(batchFacade.heroQuoteBatchJob(heroIds, batchInfo))
                .expectSubscription()
                .expectNextMatches(bi -> bi.getIdx() == batchInfo.getIdx() && bi.getStatus() == BatchStatus.RUNNING && batchApiResponseId.equals(bi.getBatchId()))
                .verifyComplete();

        // then - batchQuoteInfo list 저장 검증

        verify(batchQuoteInfoService).saveAllBatchQuoteInfoList(captor.capture());
        List<BatchQuoteInfo> captured = captor.getValue();
        assertNotNull(captured);
        assertEquals(2, captured.size()); // heroIds 수와 같아야 함
        assertTrue(captured.stream().anyMatch(bqi -> bqi.getHeroId().equals(hero1)));
        assertTrue(captured.stream().anyMatch(bqi -> bqi.getHeroId().equals(hero2)));
    }


}