package wiki.feh.externalrestdemo.hero.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.hero.domain.HeroRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class HeroServiceTest {
    @InjectMocks
    private HeroService heroService;

    @Mock
    private HeroRepository heroRepository;

    @DisplayName("getHeroesByIds")
    @Test
    void getHeroesByIds() {
        //given
        List<Hero> heroList = List.of(new Hero(), new Hero());
        List<String> ids = List.of("hero_001", "hero_002");

        doReturn(Flux.fromIterable(heroList))
                .when(heroRepository).findAllByIdIn(ids);

        //when
        var result = heroService.getHeroesByIds(ids).collectList().block();

        //then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

}