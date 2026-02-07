package wiki.feh.externalrestdemo.sqs;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSService {

    private final SQSConfig sqsConfig;

    public Mono<Void> sendMessageToSQS(String message, String queueName) {
        log.info("Sending message to SQS queue {}: {}", queueName, message);
        // Implementation for sending message to SQS

        SqsTemplate sqsTemplate = sqsConfig.sqsTemplate();

        return Mono.fromFuture(sqsTemplate.sendAsync(to -> to
                        .queue(queueName)
                        .payload(message)))
                .doOnNext(sendResult -> log.info("Message sent with ID: {}", sendResult.messageId()))
                .then();
    }

}
