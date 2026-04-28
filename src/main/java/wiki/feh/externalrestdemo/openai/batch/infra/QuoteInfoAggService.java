package wiki.feh.externalrestdemo.openai.batch.infra;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.agg.QuoteInfoAgg;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfoRepository;

import java.util.List;


@AllArgsConstructor
@Service
public class QuoteInfoAggService {
    private final BatchQuoteInfoRepository batchQuoteInfoRepository;

    @Transactional
    public Mono<Void> saveAllBatchQuoteInfoList(List<QuoteInfoAgg> quoteInfoAggList) {
        List<BatchQuoteInfo> batchQuoteInfoList = quoteInfoAggList.stream()
                .map(QuoteInfoAgg::getBatchQuoteInfo)
                .toList();
        return batchQuoteInfoRepository.batchSave(batchQuoteInfoList);
    }

}
