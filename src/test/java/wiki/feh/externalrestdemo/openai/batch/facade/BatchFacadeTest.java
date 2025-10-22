package wiki.feh.externalrestdemo.openai.batch.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.hero.service.HeroService;
import wiki.feh.externalrestdemo.heroquote.service.HeroQuoteService;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.service.BatchInfoService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BatchFacadeTest {
    @InjectMocks
    private BatchFacade batchFacade;

    @Mock
    private BatchInfoService batchInfoService;

    @Mock
    private HeroQuoteService heroQuoteService;

    @Mock
    private HeroService heroService;

    @DisplayName("requestBatchJob 테스트")
    @Test
    void requestBatchJob() {
        BatchInfo batchInfo = new BatchInfo(1, null, null, null, null);

        // given
        doReturn(Mono.just(batchInfo))
                .when(batchInfoService).saveBatchInfo(any(BatchInfo.class));

        // when
        var result = batchFacade.requestBatchJob(Flux.just("hero1", "hero2").collectList().block()).block();

        // then
        assertNotNull(result);
        assertEquals(1, result.getIdx());
    }


}