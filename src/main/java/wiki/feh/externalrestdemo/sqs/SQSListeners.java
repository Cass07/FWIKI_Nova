package wiki.feh.externalrestdemo.sqs;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import wiki.feh.externalrestdemo.openai.batch.facade.BatchFacade;
import wiki.feh.externalrestdemo.openai.bresult.controller.BResultController;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SQSListeners {

    private final BatchFacade batchFacade;
    private final SQSService sqsService;
    private final BResultController bResultController;

    @SqsListener(value = "TsHeroId", maxMessagesPerPoll = "10", factory = "sqsMessageListenerContainerFactory")
    public void listenToSQSMessages(List<Message<String>> messages) {
        for (Message<String> message : messages) {
            log.info("Received message from SQS listener: {}", message.getPayload());
            // Process the message as needed
        }

        // id list로 변환

        List<String> payloads = messages.stream()
                .map(Message::getPayload)
                .toList();

        batchFacade.requestBatchJobListener(payloads)
                .flatMap(batchInfo -> sqsService.sendMessageToSQS(batchInfo.getBatchId(), "fehwiki-tran-batchid")
                        .doOnSuccess(_ -> log.info("Sent batch-id {} to SQS", batchInfo.getBatchId()))
                        .thenReturn(batchInfo))
                .doOnNext(batchInfo -> log.info("Batch job requested with id: {}, batch-id: {}", batchInfo.getIdx(), batchInfo.getBatchId()))
                .subscribe();

    }

    @SqsListener(value = "fehwiki-tran-batchid", factory = "sqsMessageListenerContainerFactory")
    public void listenToBatchIdMessages(Message<String> messages) {
        // batch-id message 처리
        String batchId = messages.getPayload();
        log.info("Received batch-id message from SQS listener: {}", batchId);

        // 메시지 처리에 실패하면 Exception을 발생시켜서 메시지가 삭제되지 않도록 함
        bResultController.testWebhook(batchId)
                .doOnSuccess(_ -> log.info("Processed webhook for batch-id: {}", batchId))
                .doOnError(error -> {
                    log.error("Error processing webhook for batch-id {}: {}", batchId, error.getMessage());
                    throw new RuntimeException(error);
                })
                .subscribe();
    }
}
