package wiki.feh.externalrestdemo.util.logging;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DiscordMessageLoggerTest {
	@InjectMocks
	private DiscordMessageLogger discordMessageLogger;

	@Mock
	private WebClient webClient;

	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpec;

	@Mock
	private WebClient.RequestBodySpec requestBodySpec;

	@Mock
	private WebClient.ResponseSpec responseSpec;

	@Mock
	private WebClient.RequestHeadersSpec<?> requestHeadersSpec;



	@BeforeEach
	void setUp() {
		// Value 애노테이션 변수 임의 주입
		ReflectionTestUtils.setField(discordMessageLogger, "discordChannelUrl", "https://discord.com/api/webhooks/test-channel-url");
		ReflectionTestUtils.setField(discordMessageLogger, "discordUsername", "TestBot");

		// WebClient 모킹 설정
		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.bodyValue(any())).thenAnswer(invocation -> requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

	}

	// WebClient를 모킹하여 sendMessage 메서드가 올바르게 동작하는지 테스트
	@DisplayName("sendMessage 메서드가 메시지를 1900자 단위로 끊어서 전송하는지 테스트")
	@Test
	void testSendMessage() {
		// Given
		String longMessage = "a".repeat(4000); // 4000자짜리 메시지

		// When
		discordMessageLogger.sendMessage(longMessage).block();

		// Then
		// WebClient가 1900자 단위로 메시지를 전송했는지 검증
		// WebClient의 post 메서드가 3번 호출되었는지 검증 (1900 + 1900 + 1200)
		verify(webClient, times(3)).post();
		verify(requestBodySpec, times(3)).bodyValue(any());

	}

}