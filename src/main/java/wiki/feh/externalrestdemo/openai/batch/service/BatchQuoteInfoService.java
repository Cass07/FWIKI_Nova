package wiki.feh.externalrestdemo.openai.batch.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfoRepository;

import java.util.List;

@AllArgsConstructor
@Service
public class BatchQuoteInfoService {
    private final BatchQuoteInfoRepository batchQuoteInfoRepository;

    @Transactional
    public Mono<Void> saveAllBatchQuoteInfoList(List<BatchQuoteInfo> batchQuoteInfoList) {
        return batchQuoteInfoRepository.batchSave(batchQuoteInfoList);
    }
}
