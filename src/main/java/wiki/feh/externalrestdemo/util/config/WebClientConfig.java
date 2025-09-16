package wiki.feh.externalrestdemo.util.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import wiki.feh.externalrestdemo.asyncapi.service.webclient.TestAPIClient;
import wiki.feh.externalrestdemo.util.JsonTokenManager;

@Configuration
public class WebClientConfig {
    @Value("${jwt.secret}")
    private String JWT_SECRET;

    WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                        .build());
    }

    // configuration에서 JWT_SECRET를 가져오고 Webclient config 객체도 설정해서 testApiClient 객체를 만들고 이를 bean으로 등록해준다
    @Bean
    TestAPIClient testAPIClient() {
        return new TestAPIClient(
                webClientBuilder(),
                "https://feh.wiki/api/test_api.php",
                new JsonTokenManager(JWT_SECRET)
        );
    }
}
