package wiki.feh.externalrestdemo.heroquote.dto;

import wiki.feh.externalrestdemo.openai.batch.agg.QuoteInfoAgg;

public interface IHeroQuoteDtoConverter {

    String toJsonString(QuoteInfoAgg quoteInfoAgg);
}
