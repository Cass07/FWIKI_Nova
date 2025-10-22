package wiki.feh.externalrestdemo.heroquote.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteRepository;
import wiki.feh.externalrestdemo.heroquote.domain.QuoteLang;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HeroQuoteService {
    private final HeroQuoteRepository heroQuoteRepository;
    private final static QuoteLang DEFAULT_LANG = QuoteLang.JP;

    // 입력된 id 리스트에 해당하는 HeroQuote들을 모두 찾아서 id별로 묶어서 json 변환을 위한 list로 반환
    // 조회할 때 쿼리는 한 번만 실행하는 것이 유리하니까, 묶어서 조회하고 다시 나눠서 flux로 변환

    /**
     * id 리스트에 해당하는 HeroQuote들을 모두 찾아서 id별로 묶어서 json 변환을 위한 list로 반환
     */
    public Flux<Tuple2<String, List<HeroQuote>>> getQuotesAndIdByIds(List<String> ids) {
        return heroQuoteRepository.findAllByIdInAndLangOrderById(ids, DEFAULT_LANG)
                .collectList()
                // id별로 묶기
                .flatMapMany(quotes -> Flux.fromIterable(ids.stream()
                        .map(id -> Tuples.of(id, quotes.stream()
                                .filter(quote -> quote.getId().equals(id))
                                .toList()))
                        .toList()));
    }
}
