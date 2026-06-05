package wiki.feh.externalrestdemo.openai.bresult.infra.exception;

public class BatchResultFileNotExistException extends Exception {
	public BatchResultFileNotExistException(String message) {
		super(message);
	}
}
