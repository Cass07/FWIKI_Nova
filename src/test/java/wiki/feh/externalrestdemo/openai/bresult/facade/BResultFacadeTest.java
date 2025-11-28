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
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteKr;
import wiki.feh.externalrestdemo.heroquote.service.HeroQuoteKrService;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;
import wiki.feh.externalrestdemo.util.NamedLockManager;

import java.util.List;

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


        List<BResultDto.ApiResult> apiResults = List.of(
                new BResultDto.ApiResult("Home", 1, "home test1"),
                new BResultDto.ApiResult("Home", 2, "home test2"),
                new BResultDto.ApiResult("Level", 1, "level test1"),
                new BResultDto.ApiResult("Level", 2, "level test2")
        );

        // executeWithNamedLock을 모킹해서 operation인 두 번째 메소드를 리턴하도록 함
        doAnswer(invocation -> invocation.getArgument(1)).when(namedLockManager).executeWithNamedLock(
                eq("hquote_" + heroId),
                any()
        );

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
        StepVerifier.create(bResultFacade.processAndInsertHeroQuoteKr(batchQuoteInfo, apiResults))
                .expectSubscription()
                .verifyComplete();

        verify(heroQuoteKrService).batchSaveHeroQuoteKrList(heroQuoteKrListCaptor.capture());
        List<HeroQuoteKr> savedHeroQuoteKrs = heroQuoteKrListCaptor.getValue();
        assertEquals(4, savedHeroQuoteKrs.size());

        HeroQuoteKr firstHeroQuoteKr = savedHeroQuoteKrs.getFirst();

        assertEquals(65, firstHeroQuoteKr.getEditorId());
        assertEquals(6, firstHeroQuoteKr.getVersion());
        assertEquals(1, firstHeroQuoteKr.getStatus());
        assertEquals("home test1", firstHeroQuoteKr.getText());
        assertEquals(heroId, firstHeroQuoteKr.getId());

        verify(batchQuoteInfoService).updateBatchQuoteInfoComplete(batchQuoteInfoCaptor.capture());
        assertEquals(BatchStatus.COMPLETED, batchQuoteInfoCompleted.getStatus());

    }
}