package wiki.feh.externalrestdemo.heroquote.agg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;

import java.util.List;

/**
 * jpa 를 사용하지 않고 R2DBC를 사용하기 떄문에, JPA를 사용하지 않고 루트 애그리거트의 속성과 행위 등을 구현함
 */

@Getter
@AllArgsConstructor
public class HeroQuoteAgg {
    private String heroId;

    //Collect하니까 굳이 Stream 객체로 만들 필요가 없을 듯?
    private List<HeroQuote> heroQuotes;

    public HeroQuoteAgg() {
        this.heroId = null;
        this.heroQuotes = List.of();
    }
}
