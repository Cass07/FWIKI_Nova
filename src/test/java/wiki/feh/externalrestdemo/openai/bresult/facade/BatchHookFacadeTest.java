package wiki.feh.externalrestdemo.openai.bresult.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;
import wiki.feh.externalrestdemo.openai.bresult.infra.IBatchResultJsonParse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BatchHookFacadeTest {

    @InjectMocks
    private BatchHookFacade batchHookFacade;

    @Mock
    private BatchInfoService batchInfoService;

    @Mock
    private IBatchResultJsonParse batchResultJsonParse;

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
        Mono<BatchInfo> resultMono = batchHookFacade.getUpdatableBatchInfoFromBatchId(batchId);
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
        Mono<BatchInfo> resultMono = batchHookFacade.getUpdatableBatchInfoFromBatchId(batchId);
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
        Mono<BatchInfo> resultMono = batchHookFacade.getUpdatableBatchInfoFromBatchId(batchId);
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException)
                .verify();
    }

    @DisplayName("processWebhookData - 정상 케이스")
    @Test
    void processWebhookData_Success() {
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
                new BResultDto.ApiResult("Home", 1, "home test1"),
                new BResultDto.ApiResult("Level", 1, "level test1")
        )).when(batchResultJsonParse).parseResultStringToApiResultList(anyString());

        // when
        Mono<Tuple2<BatchInfo, Map<String, List<BResultDto.ApiResult>>>> resultMono =
                batchHookFacade.processWebhookData(batchInfo, jsonlList);

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
    void processWebhookData_ParseResponseJsonNull() {
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
                new BResultDto.ApiResult("Home", 1, "home test2"),
                new BResultDto.ApiResult("Level", 1, "level test2")
        )).when(batchResultJsonParse).parseResultStringToApiResultList(anyString());

        // when
        Mono<Tuple2<BatchInfo, Map<String, List<BResultDto.ApiResult>>>> resultMono =
                batchHookFacade.processWebhookData(batchInfo, jsonlList);

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
    void processWebhookData_ParseResultStringException() {
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
                new BResultDto.ApiResult("Home", 1, "home test1"),
                new BResultDto.ApiResult("Level", 1, "level test1")
        )).when(batchResultJsonParse).parseResultStringToApiResultList("successful data");

        doThrow(new RuntimeException("Parsing error"))
                .when(batchResultJsonParse).parseResultStringToApiResultList("error data");

        // when
        Mono<Tuple2<BatchInfo, Map<String, List<BResultDto.ApiResult>>>> resultMono =
                batchHookFacade.processWebhookData(batchInfo, jsonlList);

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