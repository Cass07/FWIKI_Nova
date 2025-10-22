package wiki.feh.externalrestdemo.heroquote.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteRepository;
import wiki.feh.externalrestdemo.heroquote.domain.QuoteLang;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class HeroQuoteServiceTest {
    @InjectMocks
    private HeroQuoteService heroQuoteService;

    @Mock
    private HeroQuoteRepository heroQuoteRepository;

    @DisplayName("findQuotesByIds")
    @Test
    void findQuotesByIds() {
        // given
        HeroQuote heroQuote1 = new HeroQuote(1, "test", "quote1", 1, "author1", QuoteLang.JP);
        HeroQuote heroQuote2 = new HeroQuote(2, "test", "quote2", 2, "author2", QuoteLang.JP);
        HeroQuote heroQuote3 = new HeroQuote(3, "test2", "quote1", 1, "author1", QuoteLang.JP);

        List<String> ids = List.of("test", "test2");
        QuoteLang lang = QuoteLang.JP;

        doReturn(Flux.just(heroQuote1, heroQuote2, heroQuote3))
                .when(heroQuoteRepository).findAllByIdInAndLangOrderById(ids, lang);

        // when
        var result = heroQuoteService.getQuotesAndIdByIds(ids).collectList().block();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.getFirst().getT2().size()); // "test" id에 해당하는 데이터 2개
        assertEquals(1, result.get(1).getT2().size()); // "test2" id에 해당하는 데이터 1개
    }

}