package wiki.feh.externalrestdemo.openai.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`batch_quote_result`")
public class BatchQuoteResult {
    @Id
    @Column("idx")
    private int idx;

    @Column("batch_info_id")
    private int batchInfoId;

    @Column("hero_id")
    public String heroId;

    @Column("body")
    private String body;

    @Column("created_at")
    private LocalDateTime createdAt;

}
