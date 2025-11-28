package wiki.feh.externalrestdemo.openai.batch.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfoRepository;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;

import java.util.List;

@AllArgsConstructor
@Service
public class BatchQuoteInfoService {
    private final BatchQuoteInfoRepository batchQuoteInfoRepository;

    @Transactional
    public Mono<Void> saveAllBatchQuoteInfoList(List<BatchQuoteInfo> batchQuoteInfoList) {
        return batchQuoteInfoRepository.batchSave(batchQuoteInfoList);
    }

    @Transactional
    public Mono<BatchQuoteInfo> findBatchQuoteInfoByHeroIdAndBatchInfoId(String heroId, int batchInfoId) {
        return batchQuoteInfoRepository.findFirstByHeroIdAndBatchInfoId(heroId, batchInfoId);
    }

    @Transactional
    public Flux<BatchQuoteInfo> findBatchQuoteInfoByBatchInfoId(int batchInfoId) {
        return batchQuoteInfoRepository.findByBatchInfoId(batchInfoId);
    }

    @Transactional
    public Mono<BatchQuoteInfo> updateBatchQuoteInfoFailed(BatchQuoteInfo batchQuoteInfo) {
        batchQuoteInfo.updateStatus(BatchStatus.FAILED);
        return batchQuoteInfoRepository.save(batchQuoteInfo);
    }

    @Transactional
    public Mono<BatchQuoteInfo> updateBatchQuoteInfoComplete(BatchQuoteInfo batchQuoteInfo) {
        batchQuoteInfo.updateStatus(BatchStatus.COMPLETED);
        return batchQuoteInfoRepository.save(batchQuoteInfo);
    }
}
