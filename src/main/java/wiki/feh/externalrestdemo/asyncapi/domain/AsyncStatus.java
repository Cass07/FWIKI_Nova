package wiki.feh.externalrestdemo.asyncapi.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AsyncStatus {
    PENDING("PENDING", "대기 중"),
    RUNNING("RUNNING", "진행 중"),
    COMPLETED("COMPLETED", "완료"),
    FAILED("FAILED", "실패");

    private final String key;
    private final String title;
}
