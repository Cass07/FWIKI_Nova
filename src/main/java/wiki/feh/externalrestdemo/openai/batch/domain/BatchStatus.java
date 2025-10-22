package wiki.feh.externalrestdemo.openai.batch.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 배치 작업의 상태를 나타내는 열거형
 * PENDING: Batch 작업이 생성된 상태
 * RUNNING: Batch 작업이 진행 중인 상태
 * REQUESTED: Batch 작업을 위한 전처리가 완료되어 API 요청된 상태
 * COMPLETED: Batch 작업의 결과를 받아 처리가 완료된 상태
 * FAILED: Batch 작업의 전처리, 혹은 작업의 처리가 실패한 상태
 */
@Getter
@RequiredArgsConstructor
public enum BatchStatus {
    PENDING("PENDING", "대기 중"),
    RUNNING("RUNNING", "진행 중"),
    REQUESTED("REQUESTED", "요청 완료"),
    COMPLETED("COMPLETED", "완료"),
    FAILED("FAILED", "실패");

    private final String key;
    private final String title;

    public static BatchStatus fromKey(String key) {
        for (BatchStatus status : BatchStatus.values()) {
            if (status.getKey().equals(key)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid BatchStatus key: " + key);
    }
}
