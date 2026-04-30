package wiki.feh.externalrestdemo.openai.batch.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.heroquote.agg.HeroQuoteAgg;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;
import wiki.feh.externalrestdemo.heroquote.domain.QuoteLang;
import wiki.feh.externalrestdemo.openai.batch.agg.QuoteInfoAgg;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class OpenAIBatchDtoConverterTest {
    @InjectMocks
    private OpenAIBatchDtoConverter converter;

    @DisplayName("toJsonString Test")
    @Test
    void toJsonString() {
        // given
        String hero1 = "hero_123";
        int batchInfoId = 1;
        LocalDateTime now = LocalDateTime.now();

        Hero hero1Hero = new Hero(hero1, "Hero1Kor", "HeroOneKorSub", "Hero1Jp", "HeroOneJpSub","Hero1", LocalDate.now());
        HeroQuote heroQuote1_1 = new HeroQuote(1, hero1, "Kind", 1, "quote1", QuoteLang.JP);

        HeroQuoteAgg hero1HeroQuoteAgg = new HeroQuoteAgg(hero1, List.of(heroQuote1_1));
        BatchQuoteInfo info1 = BatchQuoteInfo.builder()
                .batchInfoId(batchInfoId)
                .status(BatchStatus.REQUESTED)
                .createdAt(now)
                .heroId(hero1)
                .build();
        QuoteInfoAgg quoteInfoAgg = new QuoteInfoAgg(hero1Hero, hero1HeroQuoteAgg, info1);

        String quoteInfoAggJsonString = "{\"heroId\":\"hero_123\",\"quoteLang\":\"EN\",\"quote\":\"This is a quote.\"}";

        // when
        String jsonString = converter.toJsonString(quoteInfoAgg, quoteInfoAggJsonString);

        // then
        assertNotNull(jsonString);
        System.out.println("Generated JSON String: " + jsonString);
    }

}