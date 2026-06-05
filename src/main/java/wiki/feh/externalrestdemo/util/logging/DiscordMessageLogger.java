package wiki.feh.externalrestdemo.util.logging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class DiscordMessageLogger implements IMessageLogger {
	@Value("${discord.channel-url}")
	private String discordChannelUrl;

	@Value("${discord.username}")
	private String discordUsername;

	private final WebClient webClient;

	// 딱 메시지 전송만 구현하면 되기 떄문에 라이브러리 쓰지 않고 API 호출만 구현
	@Override
	public Mono<Void> sendMessage(String message) {
		// Discore API를 사용하여 메시지를 전송하는 로직을 구현
		// application/json POST 요청을 channelUrl로 보내되,
		// body는 {"content": message, "username": discordUsername, "avatar_url": discordAvatarUrl} 형태로 보내면 됨
		// 메시지는 최대 2000자, 1900자 단위로 끊어서 전송할 것

		// 메시지를 1900자로 끊어서 전송
		List<String> messageChunks = new ArrayList<>();
		int maxLength = 1900;
		for (int i = 0; i < message.length(); i += maxLength) {
			messageChunks.add(message.substring(i, Math.min(message.length(), i + maxLength)));
		}

		return Flux.fromIterable(messageChunks)
			.flatMap(chunk -> webClient
				.post()
				.uri(discordChannelUrl)
				.bodyValue(new DiscordMessage(chunk, discordUsername))
				.retrieve()
				.bodyToMono(Void.class))
			.then();
	}
}


