package wiki.feh.externalrestdemo.openai.batch.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfoRepository;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;

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

    /**
     * batchInfoId에 해당하는 BatchQuoteInfo 중에서 status가 PENDING인 것들을 모두 FAILED로 업데이트
     * @param batchInfoId
     * @return
     */
    @Transactional
    public Mono<Void> updateBatchQuoteInfoListStatusPendingToFailed(int batchInfoId) {
        return batchQuoteInfoRepository.updateStatusToFailedByBatchInfoIdAndStatus(batchInfoId, BatchStatus.PENDING.getKey())
                .then();
    }

    /**
     * batchInfoId에 해당하는 BatchQuoteInfo 중에서 status가 REQUESTED인 것들을 모두 FAILED로 업데이트
     * @param batchInfoId
     * @return
     */
    @Transactional
    public Mono<Void> updateBatchQuoteInfoListStatusRequestedToFailed(int batchInfoId) {
        return batchQuoteInfoRepository.updateStatusToFailedByBatchInfoIdAndStatus(batchInfoId, BatchStatus.REQUESTED.getKey())
                .then();
    }
}
