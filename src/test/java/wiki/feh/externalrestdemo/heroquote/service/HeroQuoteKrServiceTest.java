package wiki.feh.externalrestdemo.heroquote.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class HeroQuoteKrServiceTest {
    @InjectMocks
    private HeroQuoteKrService heroQuoteKrService;

    @Mock
    private HeroQuoteKrService heroQuoteKrRepository;

    @DisplayName("findLatestVersionByIdKindSeq Test")
    @Test
    void findLatestVersionByIdKindSeq() {
        // given
        String id = "hero_test";
        String kind = "JOIN";
        int seq = 1;

        // then
        assertThrows(NullPointerException.class, () -> {
            heroQuoteKrService.findLatestVersionByIdKindSeq(id, kind, seq).block();
        });
    }

}