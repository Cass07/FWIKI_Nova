package wiki.feh.externalrestdemo.heroquotekr.infra;

import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.heroquotekr.domain.HeroQuoteKr;

import java.util.List;

public interface HeroQuoteKrCustomRepository {
    Mono<Void> batchSave(List<HeroQuoteKr> heroQuoteKrList);
}
