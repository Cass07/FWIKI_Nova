package wiki.feh.externalrestdemo.heroquote.dto;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import wiki.feh.externalrestdemo.openai.batch.agg.QuoteInfoAgg;
import wiki.feh.externalrestdemo.util.json.JsonWriter;

@Component
@Qualifier("HeroQuoteDtoConverterV1")
public class HeroQuoteDtoConverterV1 implements IHeroQuoteDtoConverter {
    @Override
    public String toJsonString(QuoteInfoAgg quoteInfoAgg) {
        OpenAIHeroQuoteDtoV1.OpenAiBatchRequest openAiBatchRequest =
                OpenAIHeroQuoteDtoV1.OpenAiBatchRequest.of(quoteInfoAgg.getHero(), quoteInfoAgg.getHeroQuoteAgg());
        return JsonWriter.of(openAiBatchRequest).toString();
    }
}
