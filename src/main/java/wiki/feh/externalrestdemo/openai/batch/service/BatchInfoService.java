package wiki.feh.externalrestdemo.openai.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfoRepository;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class BatchInfoService {
    private final BatchInfoRepository batchInfoRepository;

    public Mono<BatchInfo> saveBatchInfo(BatchInfo batchInfo) {
        return batchInfoRepository.save(batchInfo)
                .doOnSuccess(saved -> {
                    // 저장 성공 시 로그 출력
                    log.info("BatchInfo saved with id: {}" , saved.getIdx());
                })
                .doOnError(error -> {
                    // 저장 실패 시 로그 출력
                    log.error("Error saving BatchInfo: {}" , error.getMessage());
                    throw new RuntimeException("Error saving BatchInfo", error);
                });
    }

    public Mono<BatchInfo> updateBatchInfoFailed(BatchInfo batchInfo) {
        batchInfo.updateStatus(BatchStatus.FAILED);
        return this.saveBatchInfo(batchInfo);
    }

    public Mono<BatchInfo> updateBatchInfoRunning(BatchInfo batchInfo) {
        batchInfo.updateStatus(BatchStatus.RUNNING);
        return this.saveBatchInfo(batchInfo);
    }

    public Mono<BatchInfo> updateBatchInfoCompleted(BatchInfo batchInfo) {
        log.info("Updating BatchInfo id: {} to COMPLETED", batchInfo.getIdx());
        batchInfo.updateStatus(BatchStatus.COMPLETED);
        return this.saveBatchInfo(batchInfo);
    }

    // BatchInfo의 batchId를 갱신하고 상태를 Requested으로 업데이트
    public Mono<BatchInfo> updateBatchInfoRequested(BatchInfo batchInfo, String batchId) {
        batchInfo.updateBatchId(batchId);
        batchInfo.updateStatus(BatchStatus.REQUESTED);
        return this.saveBatchInfo(batchInfo);
    }

    // batchId 로 BatchInfo 조회
    public Mono<BatchInfo> getBatchInfoByBatchId(String batchId) {
        return batchInfoRepository.findByBatchId(batchId);
    }
}
