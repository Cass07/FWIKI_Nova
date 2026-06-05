package wiki.feh.externalrestdemo.sqs.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpenAIBatchStatus {
	COMPLETED("COMPLETED", "완료", true),
	FAILED("FAILED", "실패", false),
	CANCELLED("CANCELLED", "취소", false),
	EXPIRED("EXPIRED", "만료", false);


	private final String key;
	private final String title;
	private final boolean isSuccess;

	public static OpenAIBatchStatus fromKey(String key) {
		for (OpenAIBatchStatus status : OpenAIBatchStatus.values()) {
			if (status.getKey().equals(key)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Invalid OpenAIBatchStatus key: " + key);
	}

	public boolean isSuccess() {
		return isSuccess;
	}
}
