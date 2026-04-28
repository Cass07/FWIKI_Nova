package wiki.feh.externalrestdemo.openai.batch.agg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.heroquote.agg.HeroQuoteAgg;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;

@Getter
@AllArgsConstructor
public class QuoteInfoAgg {
    private Hero hero;
    private HeroQuoteAgg heroQuoteAgg;
    private BatchQuoteInfo batchQuoteInfo;

    public boolean isValid() {
        return hero != null && heroQuoteAgg.isValid() && batchQuoteInfo != null;
    }
}
