package wiki.feh.externalrestdemo.openai.bresult.infra.exception;

/**
 * BatchResultJsonParse 에서 역직렬화 실패 시 발생하는 exception
 */
public class BatchResultJsonSerializeFailedException extends RuntimeException {
	public BatchResultJsonSerializeFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
