package wiki.feh.externalrestdemo.openai.batch.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfoRepository;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BatchQuoteInfoServiceTest {
    @InjectMocks
    private BatchQuoteInfoService batchQuoteInfoService;

    @Mock
    private BatchQuoteInfoRepository batchQuoteInfoRepository;

    @DisplayName("updateBatchQuoteInfoFailed 테스트")
    @Test
    void updateBatchQuoteInfoFailed_Test() {
        // given
        LocalDateTime now = LocalDateTime.now();
        String hero_id = "hero_123";
        BatchQuoteInfo info = new BatchQuoteInfo(1, 1, BatchStatus.PENDING, now, hero_id);

        doReturn(Mono.just(info.updateStatus(BatchStatus.FAILED)))
            .when(batchQuoteInfoRepository).save(info);

        // when
        BatchQuoteInfo updatedInfo = batchQuoteInfoService.updateBatchQuoteInfoFailed(info).block();

        // then
        assertNotNull(updatedInfo);
        assertEquals(BatchStatus.FAILED, updatedInfo.getStatus());
    }

    @DisplayName("updateBatchQuoteInfoComplete 테스트")
    @Test
    void updateBatchQuoteInfoComplete_Test() {
        // given
        LocalDateTime now = LocalDateTime.now();
        String hero_id = "hero_123";
        BatchQuoteInfo info = new BatchQuoteInfo(1, 1, BatchStatus.PENDING, now, hero_id);

        doReturn(Mono.just(info.updateStatus(BatchStatus.COMPLETED)))
            .when(batchQuoteInfoRepository).save(info);

        // when
        BatchQuoteInfo updatedInfo = batchQuoteInfoService.updateBatchQuoteInfoComplete(info).block();

        // then
        assertNotNull(updatedInfo);
        assertEquals(BatchStatus.COMPLETED, updatedInfo.getStatus());
    }

}