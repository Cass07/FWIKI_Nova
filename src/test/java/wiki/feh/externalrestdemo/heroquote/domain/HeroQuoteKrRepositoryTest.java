package wiki.feh.externalrestdemo.heroquote.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import wiki.feh.externalrestdemo.util.config.R2dbcConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataR2dbcTest
@EnableAutoConfiguration
@ActiveProfiles("test")
@ContextConfiguration(classes = {HeroQuoteKrRepository.class, HeroQuoteKrCustomRepository.class, R2dbcConfig.class})
class HeroQuoteKrRepositoryTest {

    @Autowired
    private HeroQuoteKrRepository heroQuoteKrRepository;

    @DisplayName("heroQuoteKr Batch Insert Test")
    @Test
    void findByHeroIdAndVersion() {
        // given
        String heroId = "hero_1";
        int version = 1;

        HeroQuoteKr dummyHeroKr = HeroQuoteKr.builder()
                .id(heroId)
                .kind("greeting")
                .seq(1)
                .text("안녕하세요!")
                .editorId(1)
                .version(version)
                .status(1)
                .build();

        heroQuoteKrRepository.batchSave(List.of(dummyHeroKr)).block();

        // when
        var maybeHeroQuoteKr = heroQuoteKrRepository.findByIdAndVersion(heroId, version).collectList().block();

        // then
        assertNotNull(maybeHeroQuoteKr);
        assertFalse(maybeHeroQuoteKr.isEmpty());
    }

    @DisplayName("findFirstByIdAndKindAndSeqOrderByVersionDesc Test -> 조회값이 없으면 0 리턴")
    @Test
    void findFirstByIdAndKindAndSeqOrderByVersionDesc_noData() {
        // given
        String heroId = "non_existing_hero";
        String kind = "greeting";
        int seq = 1;

        // when
        var result = heroQuoteKrRepository.findFirstByIdAndKindAndSeqOrderByVersionDesc(heroId, kind, seq)
                .defaultIfEmpty(HeroQuoteKr.builder().version(0).build())
                .map(HeroQuoteKr::getVersion).block();

        // then
        assertEquals(0, result);
    }

    @DisplayName("findFirstByIdAndKindAndSeqOrderByVersionDesc Test -> 조회 결과가 있으면 해당 버전 값 리턴" )
    @Test
    void findFirstByIdAndKindAndSeqOrderByVersionDesc_dataExists() {
        // given
        String heroId = "hero_2";
        String kind = "JOIN";
        int seq = 1;

        HeroQuoteKr dummyHeroKrV1 = HeroQuoteKr.builder()
                .id(heroId)
                .kind(kind)
                .seq(seq)
                .text("안녕하세요! 버전 1")
                .editorId(1)
                .version(1)
                .status(1)
                .build();

        HeroQuoteKr dummyHeroKrV2 = HeroQuoteKr.builder()
                .id(heroId)
                .kind(kind)
                .seq(seq)
                .text("안녕하세요! 버전 2")
                .editorId(1)
                .version(2)
                .status(1)
                .build();

        heroQuoteKrRepository.batchSave(List.of(dummyHeroKrV1, dummyHeroKrV2)).block();

        // when
        var result = heroQuoteKrRepository.findFirstByIdAndKindAndSeqOrderByVersionDesc(heroId, kind, seq)
                .defaultIfEmpty(HeroQuoteKr.builder().version(0).build())
                .map(HeroQuoteKr::getVersion).block();

        // then
        assertEquals(2, result);
    }
}