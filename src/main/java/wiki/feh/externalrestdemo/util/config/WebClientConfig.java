package wiki.feh.externalrestdemo.util.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import wiki.feh.externalrestdemo.service.webclient.TestAPIClient;
import wiki.feh.externalrestdemo.util.JsonTokenManager;

@Configuration
public class WebClientConfig {
    @Value("${jwt.secret}")
    private String JWT_SECRET;

    /**
     * ?
     * @return
     */
    WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                        .build());
    }

    @Bean
    TestAPIClient testAPIClient() {
        return new TestAPIClient(
                webClientBuilder(),
                "https://feh.wiki/api/test_api.php",
                new JsonTokenManager(JWT_SECRET)
        );
    }
}
