package wiki.feh.externalrestdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


/**
 * 외부 API 비동기 작업 결과를 저장하는 엔티티
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`async_result`")
public class AsyncResult {
    @Id
    @Column("id")
    private int id;

    @Column("body")
    private String body;

    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Nullable하며, 설정되어 있지 않으면 결과가 저장되지 않은 것
     */
    @Column("finished_at")
    private LocalDateTime finishedAt;

    @Column("stat")
    private AsyncStatus stat;

    public AsyncResult(int id, String body) {
        this.id = id;
        this.body = body;
        this.createdAt = LocalDateTime.now();
        this.finishedAt = null; // 초기 생성 시에는 finishedAt이 설정되지 않음
        this.stat = AsyncStatus.PENDING;
    }

    public AsyncResult(String body) {
        this.body = body;
        this.createdAt = LocalDateTime.now();
        this.finishedAt = null; // 초기 생성 시에는 finishedAt이 설정되지 않음
        this.stat = AsyncStatus.PENDING;
    }

    public void updateStatus(AsyncStatus newStatus) {
        this.stat = newStatus;
        if (newStatus == AsyncStatus.COMPLETED || newStatus == AsyncStatus.FAILED) {
            this.finishedAt = LocalDateTime.now(); // 상태가 완료되거나 실패로 변경될 때 finishedAt을 현재 시간으로 설정
        }
    }

    public void updateBody(String newBody) {
        this.body = newBody;
    }

    @Override
    public String toString() {
        return "AsyncResult{" +
                "id=" + id +
                ", body='" + body + '\'' +
                ", createdAt=" + createdAt +
                ", finishedAt=" + finishedAt +
                ", stat=" + stat +
                '}';
    }
}
