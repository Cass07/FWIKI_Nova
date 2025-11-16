package wiki.feh.externalrestdemo.heroquote.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteKr;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteKrRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class HeroQuoteKrServiceTest {
    @InjectMocks
    private HeroQuoteKrService heroQuoteKrService;

    @Mock
    private HeroQuoteKrRepository heroQuoteKrRepository;

    @DisplayName("findLatestVersionByIdKindSeq Test")
    @Test
    void findLatestVersionByIdKindSeq() {
        // given
        String id = "hero_test";
        String kind = "JOIN";
        int seq = 1;

        HeroQuoteKr heroQuoteKr = HeroQuoteKr.builder()
                .id(id)
                .kind(kind)
                .seq(seq)
                .version(2)
                .text("test")
                .editorId(1)
                .build();

        doReturn(Mono.just(heroQuoteKr))
            .when(heroQuoteKrRepository).findFirstByIdAndKindAndSeqOrderByVersionDesc(id, kind, seq);

        // when
        int version = heroQuoteKrService.findLatestVersionByIdKindSeq(id, kind, seq).block();

        // then
        assertEquals(2, version);
    }

    @DisplayName("findLatestVersionByIdKindSeq Test - No Data")
    @Test
    void findLatestVersionByIdKindSeq_NoData() {
        // given
        String id = "hero_test";
        String kind = "JOIN";
        int seq = 1;

        doReturn(Mono.empty())
            .when(heroQuoteKrRepository).findFirstByIdAndKindAndSeqOrderByVersionDesc(id, kind, seq);

        // when
        int version = heroQuoteKrService.findLatestVersionByIdKindSeq(id, kind, seq).block();

        // then
        assertEquals(0, version);
    }

    @DisplayName("findLatestVersionByIdKindSeq Test - Exception Handling")
    @Test
    void findLatestVersionByIdKindSeq_ExceptionHandling() {
        // given
        String id = "hero_test";
        String kind = "JOIN";
        int seq = 1;

        doReturn(Mono.error(new RuntimeException("DB Error")))
            .when(heroQuoteKrRepository).findFirstByIdAndKindAndSeqOrderByVersionDesc(id, kind, seq);

        // when
        int version = heroQuoteKrService.findLatestVersionByIdKindSeq(id, kind, seq).block();

        // then
        assertEquals(0, version);
    }

}