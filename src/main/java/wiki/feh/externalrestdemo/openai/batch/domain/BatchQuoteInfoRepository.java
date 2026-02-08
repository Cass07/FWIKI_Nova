package wiki.feh.externalrestdemo.openai.batch.domain;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BatchQuoteInfoRepository extends R2dbcRepository<BatchQuoteInfo, Integer>, BatchQuoteInfoCustomRepository {
    Mono<BatchQuoteInfo> findByIdx(int idx);
    Flux<BatchQuoteInfo> findByBatchInfoId(int batchInfoId);
    Mono<BatchQuoteInfo> findFirstByHeroIdAndBatchInfoId(String heroId, int batchInfoId);

    @Modifying
    @Query("UPDATE batch_quote_info SET stat = 'FAILED' WHERE batch_info_id = :batchInfoId AND stat = :status")
    Flux<BatchQuoteInfo> updateStatusToFailedByBatchInfoIdAndStatus(int batchInfoId, String status);
}
