package wiki.feh.externalrestdemo.hero.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import wiki.feh.externalrestdemo.util.config.R2dbcConfig;

import java.util.List;

@DataR2dbcTest
@EnableAutoConfiguration
@ActiveProfiles("test")
@ContextConfiguration(classes = {R2dbcConfig.class, HeroRepository.class})
class HeroRepositoryTest {
    @Autowired
    private HeroRepository heroRepository;

    @DisplayName("hero getByIdList")
    @Test
    void getByIdList() {
        // given
        var ids = List.of("hero_001", "hero_002");

        // when
        var heroes = heroRepository.findAllByIdIn(ids).collectList().block();

        // then
        assert heroes != null;
        assert heroes.size() == 2;
    }

}