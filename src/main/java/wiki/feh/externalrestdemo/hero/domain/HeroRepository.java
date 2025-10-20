package wiki.feh.externalrestdemo.hero.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface HeroRepository extends R2dbcRepository<Hero, String> {
    Flux<Hero> findAllByIdIn(List<String> ids);

}
