package wiki.feh.externalrestdemo.openai.batch.dto;

import wiki.feh.externalrestdemo.openai.batch.agg.QuoteInfoAgg;

public interface IBatchDtoConverter {
    String toJsonString(QuoteInfoAgg quoteInfoAgg, String quoteInfoAggJsonString);
}
