package wiki.feh.externalrestdemo.openai.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`batch_quote_info`")
public class BatchQuoteInfo {
    @Id
    @Column("idx")
    private int idx;

    @Column("batch_info_id")
    private int batchInfoId;

    @Column("stat")
    private BatchStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("hero_id")
    public String heroId;

    public BatchQuoteInfo updateStatus(BatchStatus status) {
        this.status = status;
        return this;
    }

    public BatchQuoteInfo updateBatchInfoId(int batchInfoId) {
        this.batchInfoId = batchInfoId;
        return this;
    }
}
