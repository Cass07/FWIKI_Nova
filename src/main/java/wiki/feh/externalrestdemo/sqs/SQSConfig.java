package wiki.feh.externalrestdemo.sqs;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@RequiredArgsConstructor
public class SQSConfig{
    @Value("${cloud.aws.credentials.access-key}")
    private String AWS_ACCESS_KEY;

    @Value("${cloud.aws.credentials.secret-key}")
    private String AWS_SECRET_KEY;

    @Value("${cloud.aws.region.static}")
    private String AWS_REGION;


    // 클라이언트 설정: region과 자격증명
    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .credentialsProvider(() -> new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return AWS_ACCESS_KEY;
                    }

                    @Override
                    public String secretAccessKey() {
                        return AWS_SECRET_KEY;
                    }
                })
                .region(Region.of(AWS_REGION))
                .build();
    }

    // listner 설정
    @Bean
    public SqsMessageListenerContainerFactory<Object> sqsMessageListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(option -> option
                        .acknowledgementMode(AcknowledgementMode.ALWAYS))
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }


    public SqsTemplate sqsTemplate() {
        return SqsTemplate.newTemplate(sqsAsyncClient());
    }

}
