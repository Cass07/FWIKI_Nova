package wiki.feh.externalrestdemo.util.json.exception;

public class JsonSerializeFailedException extends RuntimeException {
	public JsonSerializeFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
