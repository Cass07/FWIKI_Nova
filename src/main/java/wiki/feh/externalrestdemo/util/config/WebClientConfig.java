package wiki.feh.externalrestdemo.util.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

	HttpClient httpClient = HttpClient.create()
		.option(
			ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000
		);

	@Bean
	public WebClient webClient() {
		return WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
	}
}
