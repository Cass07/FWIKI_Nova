package wiki.feh.externalrestdemo.openai.batch.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.hero.service.HeroService;
import wiki.feh.externalrestdemo.heroquote.agg.HeroQuoteAgg;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;
import wiki.feh.externalrestdemo.heroquote.domain.QuoteLang;
import wiki.feh.externalrestdemo.heroquote.dto.IHeroQuoteDtoConverter;
import wiki.feh.externalrestdemo.heroquote.service.HeroQuoteService;
import wiki.feh.externalrestdemo.openai.batch.agg.QuoteInfoAgg;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.dto.IBatchDtoConverter;
import wiki.feh.externalrestdemo.openai.batch.infra.IBatchService;
import wiki.feh.externalrestdemo.openai.batch.infra.QuoteInfoAggService;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BatchFacadeTest {
    @Spy
    @InjectMocks
    private BatchFacade batchFacade;

    @Mock
    private BatchInfoService batchInfoService;

    @Mock
    private BatchQuoteInfoService batchQuoteInfoService;

    @Mock
    private QuoteInfoAggService quoteInfoAggService;

    @Mock
    private HeroQuoteService heroQuoteService;

    @Mock
    private HeroService heroService;

    @Mock
    private IBatchService openAPIBatchService;

    @Mock
    private IBatchDtoConverter batchDtoConverter;

    @Mock
    private IHeroQuoteDtoConverter heroQuoteDtoConverter;

    @Captor
    private ArgumentCaptor<List<QuoteInfoAgg>> batchQuoteInfoListCaptor;

    @Captor
    private ArgumentCaptor<List<String>> stringListCaptor;

    @DisplayName("asyncTest 테스트")
    @Test
    void testAsyncTest() {
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
    void testAsyncTestInner() {
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
    void testAsyncTestInner_Unordered() {
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
    void testRequestBatchJob() {
        // given
        BatchInfo batchInfo = new BatchInfo(1, null, BatchStatus.PENDING, LocalDateTime.now(), null);

        doReturn(Mono.just(batchInfo))
                .when(batchInfoService).saveBatchInfo(any(BatchInfo.class));
        doReturn(Mono.just(batchInfo))
                .when(batchFacade).requestHeroQuoteBatchJob(anyList(), any(BatchInfo.class));

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
    void testRequestHeroQuoteBatchJob_endToEnd_captor() {
        // given
        String hero1 = "hero1";
        String hero2 = "hero2";
        List<String> heroIds = List.of(hero1, hero2);

        Hero hero1Hero = new Hero(hero1, "Hero1Kor", "HeroOneKorSub", "Hero1Jp", "HeroOneJpSub", "Hero1", LocalDate.now());
        Hero hero2Hero = new Hero(hero2, "Hero2Kor", "HeroTwoKorSub", "Hero2Jp", "HeroTwoJpSub", "Hero2", LocalDate.now());

        HeroQuote q11 = new HeroQuote(1, hero1, "Kind", 1, "quote1", QuoteLang.JP);
        HeroQuote q12 = new HeroQuote(2, hero1, "Brave", 1, "quote2", QuoteLang.JP);
        HeroQuote q21 = new HeroQuote(3, hero2, "Kind", 1, "quote3", QuoteLang.JP);
        HeroQuote q22 = new HeroQuote(4, hero2, "Brave", 1, "quote4", QuoteLang.JP);

        BatchInfo seed = new BatchInfo(1, null, BatchStatus.PENDING, LocalDateTime.now(), null);
        String batchApiResponseId = "external-batch-id-123";

        doReturn(Flux.just(hero1Hero, hero2Hero)).when(heroService).getHeroesByIds(heroIds);
        doReturn(Flux.just(
            new HeroQuoteAgg(hero1, List.of(q11, q12)),
            new HeroQuoteAgg(hero2, List.of(q21, q22))
        )).when(heroQuoteService).getHeroQuoteAggByIds(heroIds);

        doReturn(Mono.empty()).when(quoteInfoAggService).saveAllBatchQuoteInfoList(anyList());
        doReturn("{\"quote\":\"json\"}").when(heroQuoteDtoConverter).toJsonString(any());
        doReturn("{\"wrapped\":true}").when(batchDtoConverter).toJsonString(any(), anyString());
        doReturn(Mono.just(batchApiResponseId)).when(openAPIBatchService).callRequestBatchApi(anyList());

        // 새 객체를 만들어 반환해서 상태 오염 방지
        doAnswer(inv -> {
            BatchInfo in = inv.getArgument(0);
            String bid = inv.getArgument(1);
            return Mono.just(new BatchInfo(in.getIdx(), bid, BatchStatus.REQUESTED, in.getCreatedAt(), in.getUpdatedAt()));
        }).when(batchInfoService).updateBatchInfoRequested(any(BatchInfo.class), eq(batchApiResponseId));

        // when / then
        StepVerifier.create(batchFacade.requestHeroQuoteBatchJob(heroIds, seed))
            .assertNext(bi -> {
                // 실패 시 어떤 필드가 다른지 바로 보이게 메시지 포함
                assertEquals(seed.getIdx(), bi.getIdx(), "idx mismatch");
                assertEquals(BatchStatus.REQUESTED, bi.getStatus(), "status mismatch");
                assertEquals(batchApiResponseId, bi.getBatchId(), "batchId mismatch");
            })
            .verifyComplete();

        // 추가적으로 저장된 BatchQuoteInfo 리스트의 heroId가 올바른지 검증
        verify(quoteInfoAggService).saveAllBatchQuoteInfoList(batchQuoteInfoListCaptor
            .capture());
        List<QuoteInfoAgg> capturedAggs = batchQuoteInfoListCaptor.getValue();
        assertNotNull(capturedAggs, "Captured QuoteInfoAgg list is null");
        assertEquals(2, capturedAggs.size(), "Captured QuoteInfoAgg list size mismatch");
        assertTrue(capturedAggs.stream().anyMatch(agg -> agg.getHero().getId().equals(hero1)), "Hero1 not found in captured QuoteInfoAgg list");
        assertTrue(capturedAggs.stream().anyMatch(agg -> agg.getHero().getId().equals(hero2)), "Hero2 not found in captured QuoteInfoAgg list");

        // 추가적으로 callRequestBatchApi에 전달된 JSON 리스트의 내용이 올바른지 검증
        verify(openAPIBatchService).callRequestBatchApi(stringListCaptor.capture());
        List<String> capturedJsonList = stringListCaptor.getValue();
        assertNotNull(capturedJsonList, "Captured JSON list is null");
        assertEquals(2, capturedJsonList.size(), "Captured JSON list size mismatch");
    }
}