package wiki.feh.externalrestdemo.openai.batch.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import wiki.feh.externalrestdemo.openai.batch.agg.QuoteInfoAgg;

@Slf4j
@Component
@Qualifier("OpenAIBatchDtoConverter")
public class OpenAIBatchDtoConverter implements IBatchDtoConverter {

    @Override
    public String toJsonString(QuoteInfoAgg quoteInfoAgg, String quoteInfoAggJsonString) {
        OpenAIBatchDtoV1.BatchRequestLine batchRequestLine = new OpenAIBatchDtoV1.BatchRequestLine(
                quoteInfoAgg.getHero().getId(),
                quoteInfoAggJsonString);


        return batchRequestLine.toJsonString();
    }
}
