package wiki.feh.externalrestdemo.openai.batch.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BatchInfoServiceTest {
    @InjectMocks
    private BatchInfoService batchInfoService;

    @Mock
    private BatchInfoRepository batchInfoRepository;

    @DisplayName("saveBatchInfo Success")
    @Test
    void saveBatchInfo_Success() {
        // given
        String batchId = "batch_123";
        BatchInfo batchInfo = new BatchInfo(batchId);
        BatchInfo resultBatchInfo = BatchInfo.builder()
                .idx(1)
                .batchId(batchId)
                .status(batchInfo.getStatus())
                .createdAt(batchInfo.getCreatedAt())
                .updatedAt(batchInfo.getUpdatedAt())
                .build();

        doReturn(Mono.just(resultBatchInfo))
            .when(batchInfoRepository).save(batchInfo);

        // when
        BatchInfo saved = batchInfoService.saveBatchInfo(batchInfo).block();

        // then
        assertNotNull(saved);
        assertEquals(1, saved.getIdx());
    }

    @DisplayName("saveBatchInfo error")
    @Test
    void saveBatchInfo_Error() {
        // given
        String batchId = "batch_123";
        BatchInfo batchInfo = new BatchInfo(batchId);

        doReturn(Mono.error(new RuntimeException("DB error")))
            .when(batchInfoRepository).save(batchInfo);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> batchInfoService.saveBatchInfo(batchInfo).block());

        assertEquals("Error saving BatchInfo", exception.getMessage());
    }

    @DisplayName("updateBatchInfoFailed updates status to FAILED")
    @Test
    void updateBatchInfoFailed_UpdatesStatusToFailed() {
        // given
        String batchId = "batch_123";
        BatchInfo batchInfo = new BatchInfo(batchId);

        BatchInfo updatedBatchInfo = BatchInfo.builder()
                .idx(1)
                .batchId(batchId)
                .status(wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus.FAILED)
                .createdAt(batchInfo.getCreatedAt())
                .updatedAt(batchInfo.getUpdatedAt())
                .build();

        doReturn(Mono.just(updatedBatchInfo))
            .when(batchInfoRepository).save(any(BatchInfo.class));

        // when
        BatchInfo result = batchInfoService.updateBatchInfoFailed(batchInfo).block();

        // then
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus().name());
    }

    @DisplayName("updateBatchInfoRunning updates batchId and status to RUNNING")
    @Test
    void updateBatchInfoRunning_UpdatesBatchIdAndStatusToRunning() {
        // given
        String initialBatchId = "batch_123";
        String newBatchId = "batch_456";
        BatchInfo batchInfo = new BatchInfo(initialBatchId);

        BatchInfo updatedBatchInfo = BatchInfo.builder()
                .idx(1)
                .batchId(newBatchId)
                .status(wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus.RUNNING)
                .createdAt(batchInfo.getCreatedAt())
                .updatedAt(batchInfo.getUpdatedAt())
                .build();

        doReturn(Mono.just(updatedBatchInfo))
            .when(batchInfoRepository).save(any(BatchInfo.class));

        // when
        BatchInfo result = batchInfoService.updateBatchInfoRunning(batchInfo, newBatchId).block();

        // then
        assertNotNull(result);
        assertEquals(newBatchId, result.getBatchId());
        assertEquals("RUNNING", result.getStatus().name());
    }

}