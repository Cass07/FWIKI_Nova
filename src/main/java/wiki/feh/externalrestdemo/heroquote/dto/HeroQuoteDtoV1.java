package wiki.feh.externalrestdemo.heroquote.dto;

import lombok.Getter;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.heroquote.agg.HeroQuoteAgg;
import wiki.feh.externalrestdemo.util.json.JsonWriter;

@Getter
public class HeroQuoteDtoV1 implements IHeroQuoteDto {
    OpenAIHeroQuoteDtoV1.OpenAiBatchRequest openAiBatchRequest;

    public HeroQuoteDtoV1(Hero hero, HeroQuoteAgg heroQuoteAgg) {
        this.openAiBatchRequest = OpenAIHeroQuoteDtoV1.OpenAiBatchRequest.of(hero, heroQuoteAgg);
    }

    @Override
    public String generateJsonBody() {
        return JsonWriter.of(this.openAiBatchRequest).toString();
    }
}
