package wiki.feh.externalrestdemo.hero.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.hero.domain.HeroRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HeroService {
    private final HeroRepository heroRepository;

    // hero id list로 hero list 조회
    Flux<Hero> getHeroesByIds(List<String> ids) {
        return heroRepository.findAllByIdIn(ids);
    }
}
