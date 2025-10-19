package wiki.feh.externalrestdemo.heroquote.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface HeroQuoteRepository extends R2dbcRepository<HeroQuote, Integer> {
    Flux<HeroQuote> findById(String id);
    Flux<HeroQuote> findByIdInAndLangOrderById(List<String> ids, QuoteLang lang);
}
