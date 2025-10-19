package wiki.feh.externalrestdemo.heroquote.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import wiki.feh.externalrestdemo.util.config.R2dbcConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@EnableAutoConfiguration
@ActiveProfiles("test")
@ContextConfiguration(classes = {R2dbcConfig.class, HeroQuoteRepository.class})
class HeroQuoteRepositoryTest {
    @Autowired
    private HeroQuoteRepository heroQuoteRepository;

    @DisplayName("heroQuote getById")
    @Test
    void getById() {
        // given
        String id = "test";

        // when
        var heroQuotes = heroQuoteRepository.findById(id).collectList().block();

        // then
        assertNotNull(heroQuotes);
        assertFalse(heroQuotes.isEmpty());
        assertEquals("test", heroQuotes.getFirst().getId());
    }
}