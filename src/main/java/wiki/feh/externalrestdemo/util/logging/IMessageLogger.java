package wiki.feh.externalrestdemo.util.logging;

import reactor.core.publisher.Mono;

public interface IMessageLogger {
	Mono<Void> sendMessage(String message);
}
