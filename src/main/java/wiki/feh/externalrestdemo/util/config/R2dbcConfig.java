package wiki.feh.externalrestdemo.util.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import wiki.feh.externalrestdemo.heroquote.domain.QuoteLangConverter;

import java.util.List;


@EnableR2dbcRepositories
@Configuration
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}" )
    private String url;

    @Value("${spring.r2dbc.username}" )
    private String username;

    @Value("${spring.r2dbc.password}" )
    private String password;

    @Bean
    @Override
    public @NonNull ConnectionFactory connectionFactory() {
        int processors = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = Math.max(10, processors * 2 + 1);
        System.out.println("pool size: " + maxPoolSize);

        ConnectionFactory cf = io.r2dbc.spi.ConnectionFactories.get(
                io.r2dbc.spi.ConnectionFactoryOptions.parse(url)
                        .mutate()
                        .option(io.r2dbc.spi.ConnectionFactoryOptions.USER, username)
                        .option(io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD, password)
                        .build()
        );

        ConnectionPoolConfiguration config = ConnectionPoolConfiguration.builder()
                .connectionFactory(cf)
                .maxSize(maxPoolSize)
                .initialSize(10)
                .build();

        return new ConnectionPool(config);
    }

    @Override
    protected @NonNull List<Object> getCustomConverters() {
        return List.of(new QuoteLangConverter());
    }
}
