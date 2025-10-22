package wiki.feh.externalrestdemo.openai.batch.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class BatchQuoteInfoRepositoryImpl implements BatchQuoteInfoCustomRepository {

    private final DatabaseClient databaseClient;
    @Override
    public Mono<Void> batchSave(List<BatchQuoteInfo> batchQuoteInfoList) {
        return databaseClient.inConnection(conn ->
                Mono.from(
                        // list를 돌면서 insert 쿼리를 만들어서 실행
                        conn.createStatement("INSERT INTO batch_quote_info (batch_info_id, hero_id, stat, created_at) VALUES " +
                                String.join(", ",
                                        batchQuoteInfoList.stream()
                                                .map(this::batchQuoteInfoToValueString)
                                                .toList()
                                )
                        )
                        .execute())
                        .then()); // 완료 신호만 반환
                        // 실행 결과를 BatchQuoteInfo 객체로 매핑 - MySQL에서는 Returning이 지원되지 않으므로, 아무것도 반환하지 않는 것으로 변경함
                        //.flatMap(result -> result.map(this::mapRowToBatchQuoteInfo)));
    }

    private String batchQuoteInfoToValueString(BatchQuoteInfo bq) {
        return String.format("(%d, '%s', '%s', '%s')",
                bq.getBatchInfoId(),
                bq.getHeroId().replace("'", "''"), // 작은따옴표 이스케이프 처리 필요
                bq.getStatus().getKey(),
                bq.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

//    private BatchQuoteInfo mapRowToBatchQuoteInfo(io.r2dbc.spi.Readable row) {
//        return BatchQuoteInfo.builder()
//                .idx(row.get("idx", Integer.class))
//                .batchInfoId(row.get("batch_info_id", Integer.class))
//                .heroId(row.get("hero_id", String.class))
//                .status(BatchStatus.fromKey(row.get("status", String.class)))
//                .build();
//    }
}
