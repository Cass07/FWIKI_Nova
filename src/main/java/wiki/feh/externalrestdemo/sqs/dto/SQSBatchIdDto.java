package wiki.feh.externalrestdemo.sqs.dto;

/**
 * @param id - webhook 이벤트에서 큐로 전달한 batchId
 * @param status - "completed", "failed", "cancelled", "expired" 중 하나의 상태를 가짐
 */
public record SQSBatchIdDto (String id, OpenAIBatchStatus status){

	public boolean isValid() {
		if (id == null || id.isBlank()) {
			return false;
		}
		try {
			OpenAIBatchStatus.fromKey(status.getKey());
		} catch (IllegalArgumentException _) {
			return false;
		}
		return true;
	}
}
