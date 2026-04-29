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
import wiki.feh.externalrestdemo.heroquotekr.domain.HeroQuoteKr;
import wiki.feh.externalrestdemo.heroquotekr.service.HeroQuoteKrService;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;
import wiki.feh.externalrestdemo.openai.bresult.dto.ApiResultV1;
import wiki.feh.externalrestdemo.util.NamedLockManager;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}