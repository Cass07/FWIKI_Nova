package wiki.feh.externalrestdemo.heroquotekr.infra;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.heroquotekr.domain.HeroQuoteKr;

@Repository
public interface HeroQuoteKrRepository extends R2dbcRepository<HeroQuoteKr, Integer>, HeroQuoteKrCustomRepository {
    Flux<HeroQuoteKr> findByIdAndVersion(String id, int version);
    Mono<HeroQuoteKr> findFirstByIdAndKindAndSeqOrderByVersionDesc(String id, String kind, int seq);
}
