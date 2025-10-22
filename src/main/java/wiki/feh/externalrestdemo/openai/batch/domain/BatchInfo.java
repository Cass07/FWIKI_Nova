package wiki.feh.externalrestdemo.openai.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Table(name = "`batch_info`")
public class BatchInfo {
    @Id
    @Column("idx")
    private int idx;

    @Column("batch_id")
    private String batchId;

    @Column("stat")
    private BatchStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public BatchInfo() {
        this.batchId = "";
        this.status = BatchStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BatchInfo(String batchId) {
        this.batchId = batchId;
        this.status = BatchStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BatchInfo(String batchId, BatchStatus status) {
        this.batchId = batchId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BatchInfo updateStatus(BatchStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public BatchInfo updateBatchId(String batchId) {
        this.batchId = batchId;
        this.updatedAt = LocalDateTime.now();
        return this;
    }
}
