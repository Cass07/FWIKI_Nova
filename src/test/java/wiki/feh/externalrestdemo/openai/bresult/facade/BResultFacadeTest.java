package wiki.feh.externalrestdemo.openai.bresult.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.heroquotekr.domain.HeroQuoteKr;
import wiki.feh.externalrestdemo.heroquotekr.service.HeroQuoteKrService;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;
import wiki.feh.externalrestdemo.openai.bresult.dto.ApiResultV1;
import wiki.feh.externalrestdemo.openai.bresult.dto.IApiResult;
import wiki.feh.externalrestdemo.openai.bresult.infra.IBatchResultJsonParse;
import wiki.feh.externalrestdemo.util.NamedLockManager;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BResultFacadeTest {

    @InjectMocks
    private BResultFacade bResultFacade;

    @Mock
    private HeroQuoteKrService heroQuoteKrService;

    @Mock
    private NamedLockManager namedLockManager;

    @Mock
    private BatchQuoteInfoService batchQuoteInfoService;

    @Mock
    private BatchInfoService batchInfoService;

    @Mock
    private IBatchResultJsonParse batchResultJsonParse;

    @Captor
    private ArgumentCaptor<List<HeroQuoteKr>> heroQuoteKrListCaptor;

    @Captor
    private ArgumentCaptor<BatchQuoteInfo> batchQuoteInfoCaptor;

    @DisplayName("insertHeroQuoteKr 테스트")
    @Test
    void insertHeroQuoteKr() {
        // given
        String heroId = "hero_123";
        int currentIndex = 5;
        BatchQuoteInfo batchQuoteInfo = new BatchQuoteInfo(1, 1, BatchStatus.PENDING, null, heroId);
        BatchQuoteInfo batchQuoteInfoCompleted = new BatchQuoteInfo(1, 1, BatchStatus.COMPLETED, null, heroId);


        List<ApiResultV1> apiResults = List.of(
                new ApiResultV1("Home", 1, "home test1"),
                new ApiResultV1("Home", 2, "home test2"),
                new ApiResultV1("Level", 1, "level test1"),
                new ApiResultV1("Level", 2, "level test2")
        );

        // executeWithNamedLock을 모킹해서 operation인 두 번째 메소드를 리턴하도록 함
        doAnswer(invocation -> {
            Supplier<Mono<Void>> operation = invocation.getArgument(1);
            return operation.get(); // Supplier를 실행해서 Mono를 꺼내야 함
        }).when(namedLockManager).executeWithNamedLock(eq("hquote_" + heroId), any());

        doReturn(Mono.empty())
                .when(heroQuoteKrService)
                .batchSaveHeroQuoteKrList(anyList());

        doReturn(Mono.just(currentIndex))
                .when(heroQuoteKrService)
                .getLatestIndexForHeroQuoteKr(eq(heroId));


        doReturn(Mono.just(batchQuoteInfoCompleted))
                .when(batchQuoteInfoService)
                .updateBatchQuoteInfoComplete(batchQuoteInfo);


        // then
        StepVerifier.create(bResultFacade.lockHeroIdAndInsertHeroQuoteKr(batchQuoteInfo, apiResults))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();

        verify(heroQuoteKrService).batchSaveHeroQuoteKrList(heroQuoteKrListCaptor.capture());
        List<HeroQuoteKr> savedHeroQuoteKrs = heroQuoteKrListCaptor.getValue();
        assertEquals(4, savedHeroQuoteKrs.size());

        HeroQuoteKr firstHeroQuoteKr = savedHeroQuoteKrs.getFirst();

        assertEquals(65, firstHeroQuoteKr.getEditorId());
        assertEquals(6, firstHeroQuoteKr.getVersion());
        assertEquals(2, firstHeroQuoteKr.getStatus());
        assertEquals("home test1", firstHeroQuoteKr.getText());
        assertEquals(heroId, firstHeroQuoteKr.getId());

        verify(batchQuoteInfoService).updateBatchQuoteInfoComplete(batchQuoteInfoCaptor.capture());
        assertEquals(BatchStatus.COMPLETED, batchQuoteInfoCompleted.getStatus());

    }

    @DisplayName("verifyBatchId - requested 상태인 경우 성공")
    @Test
    void getUpdatableBatchInfoFromBatchId_Success() {
        // given
        String batchId = "batch_123";
        BatchInfo batchInfo = BatchInfo.builder()
                .idx(1)
                .batchId(batchId)
                .status(wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus.REQUESTED)
                .createdAt(null)
                .updatedAt(null)
                .build();

        doReturn(Mono.just(batchInfo))
                .when(batchInfoService).getBatchInfoByBatchId(batchId);

        // when, then
        Mono<BatchInfo> resultMono = bResultFacade.getUpdatableBatchInfoFromBatchId(batchId);
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertEquals(batchInfo.getBatchId(), result.getBatchId());
                    assertEquals(batchInfo.getStatus(), result.getStatus());
                })
                .verifyComplete();
    }

    @DisplayName("verifyBatchId - 상태가 requested가 아닌 경우 에러")
    @Test
    void getUpdatableBatchInfoFromBatchId_InvalidStatus() {
        // given
        String batchId = "batch_123";
        BatchInfo batchInfo = BatchInfo.builder()
                .idx(1)
                .batchId(batchId)
                .status(wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus.COMPLETED)
                .createdAt(null)
                .updatedAt(null)
                .build();

        doReturn(Mono.just(batchInfo))
                .when(batchInfoService).getBatchInfoByBatchId(batchId);

        // when, then
        Mono<BatchInfo> resultMono = bResultFacade.getUpdatableBatchInfoFromBatchId(batchId);
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException)
                .verify();
    }

    @DisplayName("verifyBatchId - batchId에 해당하는 BatchInfo가 없는 경우 에러")
    @Test
    void verifyBatchId_Updatable_BatchInfoNotFound() {
        // given
        String batchId = "batch_123";

        doReturn(Mono.empty())
                .when(batchInfoService).getBatchInfoByBatchId(batchId);

        // when, then
        Mono<BatchInfo> resultMono = bResultFacade.getUpdatableBatchInfoFromBatchId(batchId);
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException)
                .verify();
    }

    @DisplayName("processWebhookData - 정상 케이스")
    @Test
    void makeApiResultMapFromBatchInfoAndJsonList_Success() {
        // given
        String batchId = "batch_123";
        BatchInfo batchInfo = BatchInfo.builder()
                .idx(1)
                .batchId(batchId)
                .status(wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus.REQUESTED)
                .createdAt(null)
                .updatedAt(null)
                .build();

        List<String> jsonlList = List.of(
                "test1",
                "test2"
        );

        doReturn(Tuples.of("hero_1", "some other data"))
                .when(batchResultJsonParse).parseResponseJson(jsonlList.get(0));
        doReturn(Tuples.of("hero_2", "some other data"))
                .when(batchResultJsonParse).parseResponseJson(jsonlList.get(1));

        doReturn(List.of(
                new ApiResultV1("Home", 1, "home test1"),
                new ApiResultV1("Level", 1, "level test1")
        )).when(batchResultJsonParse).parseResultStringToApiResultList(anyString(), any());

        // when
        Mono<Tuple2<BatchInfo, Map<String, List<? extends IApiResult>>>> resultMono =
                bResultFacade.makeApiResultMapFromBatchInfoAndJsonList(batchInfo, jsonlList);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertEquals(batchInfo, result.getT1());
                    assertTrue(result.getT2().containsKey("hero_1"));
                    assertTrue(result.getT2().containsKey("hero_2"));
                    assertEquals(2, result.getT2().get("hero_1").size());
                    assertEquals(2, result.getT2().get("hero_2").size());
                })
                .verifyComplete();
    }

    @DisplayName("processWebhookData - parseResponseJson가 null을 반환하는 경우만 무시됨")
    @Test
    void makeApiResultListFromBatchInfoAndJsonMap_ParseResponseJsonNull() {
        // given
        String batchId = "batch_123";
        BatchInfo batchInfo = BatchInfo.builder()
                .idx(1)
                .batchId(batchId)
                .status(wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus.REQUESTED)
                .createdAt(null)
                .updatedAt(null)
                .build();

        List<String> jsonlList = List.of(
                "test1",
                "test2"
        );

        doReturn(null)
                .when(batchResultJsonParse).parseResponseJson(jsonlList.get(0));
        doReturn(Tuples.of("hero_2", "some other data"))
                .when(batchResultJsonParse).parseResponseJson(jsonlList.get(1));

        doReturn(List.of(
                new ApiResultV1("Home", 1, "home test2"),
                new ApiResultV1("Level", 1, "level test2")
        )).when(batchResultJsonParse).parseResultStringToApiResultList(anyString(), any());

        // when
        Mono<Tuple2<BatchInfo, Map<String, List<? extends IApiResult>>>> resultMono =
                bResultFacade.makeApiResultMapFromBatchInfoAndJsonList(batchInfo, jsonlList);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertEquals(batchInfo, result.getT1());
                    assertFalse(result.getT2().containsKey("hero_1"));
                    assertTrue(result.getT2().containsKey("hero_2"));
                    assertEquals(2, result.getT2().get("hero_2").size());
                })
                .verifyComplete();
    }

    @DisplayName("processWebhookData - parseResultStringToApiResultList가 예외를 던지는 경우 무시됨")
    @Test
    void makeApiResultListFromBatchInfoAndJsonList_ParseResultStringException() {
        // given
        String batchId = "batch_123";
        BatchInfo batchInfo = BatchInfo.builder()
                .idx(1)
                .batchId(batchId)
                .status(wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus.REQUESTED)
                .createdAt(null)
                .updatedAt(null)
                .build();

        List<String> jsonlList = List.of(
                "test1",
                "test2"
        );

        doReturn(Tuples.of("hero_1", "successful data"))
                .when(batchResultJsonParse).parseResponseJson(jsonlList.get(0));
        doReturn(Tuples.of("hero_2", "error data"))
                .when(batchResultJsonParse).parseResponseJson(jsonlList.get(1));

        doReturn(List.of(
                new ApiResultV1("Home", 1, "home test1"),
                new ApiResultV1("Level", 1, "level test1")
        )).when(batchResultJsonParse).parseResultStringToApiResultList(eq("successful data"), any());

        doThrow(new RuntimeException("Parsing error"))
                .when(batchResultJsonParse).parseResultStringToApiResultList(eq("error data"), any());

        // when
        Mono<Tuple2<BatchInfo, Map<String, List<? extends IApiResult>>>> resultMono =
                bResultFacade.makeApiResultMapFromBatchInfoAndJsonList(batchInfo, jsonlList);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertEquals(batchInfo, result.getT1());
                    assertTrue(result.getT2().containsKey("hero_1"));
                    assertFalse(result.getT2().containsKey("hero_2"));
                    assertEquals(2, result.getT2().get("hero_1").size());
                })
                .verifyComplete();
    }
}