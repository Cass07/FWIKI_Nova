package wiki.feh.externalrestdemo.heroquote.domain;

import reactor.core.publisher.Mono;

import java.util.List;

public interface HeroQuoteKrCustomRepository {
    Mono<Void> batchSave(List<HeroQuoteKr> heroQuoteKrList);
}
