package wiki.feh.externalrestdemo.sqs;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import io.awspring.cloud.sqs.listener.acknowledgement.BatchAcknowledgement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.batch.controller.BatchController;
import wiki.feh.externalrestdemo.openai.bresult.controller.BResultController;
import wiki.feh.externalrestdemo.sqs.dto.SQSBatchIdDto;
import wiki.feh.externalrestdemo.util.logging.IMessageLogger;

@Slf4j
@Component
@RequiredArgsConstructor
public class SQSListeners {
	private final BatchController batchController;
	private final SQSService sqsService;
	private final BResultController bResultController;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Qualifier("discordMessageLogger")
	private final IMessageLogger messageLogger;

	/**
	 * TsHeroId queue에서 메시지를 받아서 BatchController.requestBatchJob을 호출해 Batch Job을 요청한다
	 * @param messages - SQS에서 받은 메시지 객체 리스트. payload는 TsHeroId의 id 값이 담긴 문자열
	 * @param acknowledgement - 메시지 소비를 제어하는 객체
	 */
	@SqsListener(value = "TsHeroId", maxMessagesPerPoll = "10", factory = "sqsMessageListenerContainerFactory")
	public void listenToSQSTsHeroMessages(List<Message<String>> messages,
		BatchAcknowledgement<String> acknowledgement) {
		log.info("Received {} messages from SQS listener", messages.size());
		for (Message<String> message : messages) {
			log.info("Received message from SQS listener: {}", message.getPayload());
		}

		// id list로 변환

		List<String> payloads = messages.stream()
			.map(Message::getPayload)
			.toList();

		batchController.requestBatchJob(payloads)
			// .flatMap(batchInfo -> sqsService.sendMessageToSQS(batchInfo.getBatchId(), "fehwiki-tran-batchid")
			// 	.doOnSuccess(_ -> log.info("Sent batch-id {} to SQS", batchInfo.getBatchId()))
			// 	.thenReturn(batchInfo))
			.doOnNext(batchInfo -> log.info("Batch job requested with id: {}, batch-id: {}", batchInfo.getIdx(),
				batchInfo.getBatchId()))
			.then(Mono.fromFuture(acknowledgement.acknowledgeAsync())
				.then())
			.doOnSuccess(_ -> log.info("Acknowledged {} messages", messages.size()))
			.doOnError(error -> log.error("Error processing messages: {}", error.getMessage()))
			.subscribe();

	}

	//@SqsListener(value = "fehwiki-tran-batchid", factory = "sqsMessageListenerContainerFactory")
	public void listenToBatchIdMessages(Message<String> messages, Acknowledgement acknowledgement) {
		// batch-id message 처리
		String batchId = messages.getPayload();
		log.info("Received batch-id message from SQS listener: {}", batchId);

		// 메시지 처리에 실패하면 Exception을 발생시켜서 메시지가 삭제되지 않도록 함
		bResultController.updateTranslateData(batchId)
			.then(Mono.defer(() -> Mono.fromFuture(acknowledgement.acknowledgeAsync())
				.then()))
			.doOnSuccess(_ -> log.info("Processed webhook for batch-id: {}", batchId))
			.doOnError(error -> log.error("Error processing webhook for batch-id {}: {}", batchId, error.getMessage()))
			.subscribe();
	}

	/**
	 * tran-batchId queue에서 메시지를 받아서 처리한다
	 * 메시지가 역직렬화 불가능하면 메시지를 소비하고 에러 로그를 보내고,
	 * 에러 발생 없이 메시지를 처리했다면 Acknowledgement.acknowledgeAsync()를 호출해서 메시지를 소비한다
	 * 오류가 발생한 경우에는 메시지를 소비하지 않고 에러 로그만을 보낸다
	 * @param messages - SQS에서 받은 메시지 객체. payload는 batchId와 status를 포함하는 JSON 문자열
	 * @param acknowledgement - 메시지 소비를 제어하는 객체
	 */
	@SqsListener(value = "fehwiki-tran-batchid", factory = "sqsMessageListenerContainerFactory")
	public void listenToBatchIdMessagesV2(Message<String> messages, Acknowledgement acknowledgement) {
		String payload = messages.getPayload();

		// payload를 역직렬화해 SQSBatchIdDto 객체로 변환
		deserializeBatchIdMessage(payload, acknowledgement)
			.flatMap(dto -> {
				if (!dto.isValid()) {
					return Mono.error(new RuntimeException("Invalid batch-id in message payload"));
				}

				if (dto.status().isSuccess()) {
					log.info("Received valid batch-id message from SQS listener with status {}: {}", dto.status().getKey(),
						dto.id());
					return bResultController.updateTranslateData(dto.id());
				}

				log.info("Received batch-id message with non-success status {}: {}", dto.status().getKey(), dto.id());
				return bResultController.updateBatchAsFailed(dto.id());
			})
			.then(Mono.defer(() -> Mono.fromFuture(acknowledgement.acknowledgeAsync())
				.then()))
			.onErrorResume(error -> {
				log.error("Update failed for batch-id {}: {}", payload, error.getMessage());
				// 에러 메시지를 전송하고 에러 전파를 멈춘다
				return messageLogger.sendMessage(
					"Error processing batch-id " + payload + ": " + error.getMessage());
			})
			.doOnSuccess(_ -> log.info("Processed webhook for batch-id: {}", payload))
			.doOnError(error -> log.error("Error processing webhook for batch-id {}: {}", payload, error.getMessage()))
			.subscribe();
	}

	/**
	 * 메시지 payload를 SQSBatchIdDto로 역직렬화하고
	 * 역직렬화에 실패하면 에러 메시지를 전송하고, 메시지를 삭제하도록 acknowledgeAsync를 호출하는 Mono를 반환한다
	 * @param payload - SQS 메시지의 payload
	 * @param acknowledgement - 해당 Queue의 Acknowledgement 객체
	 * @return
	 */
	private Mono<SQSBatchIdDto> deserializeBatchIdMessage(String payload, Acknowledgement acknowledgement) {
		return Mono.fromCallable(() -> objectMapper.readValue(payload, SQSBatchIdDto.class))
			.onErrorResume(JsonProcessingException.class, e -> {
				log.error("Failed to deserialize message payload: {}", payload, e);
				// 역직렬화 실패 시 에러 메시지를 전송하고 에러 전파를 멈춘다
				return messageLogger.sendMessage(
						"Failed to deserialize message payload: " + payload + ". Error: " + e.getMessage())
					.onErrorResume(_ -> {
						log.error("Failed to send error message for deserialization failure: {}", e.getMessage());
						return Mono.empty();
					})
					.then(Mono.fromFuture(acknowledgement.acknowledgeAsync())
						.then(Mono.empty()));
			});
	}

	// queue test용
	//@SqsListener(value = "TsHeroId", factory = "sqsMessageListenerContainerFactory")
	public void listenToBatchIdTestMessages(Message<String> messages, Acknowledgement acknowledgement) {
		String messagePayload = messages.getPayload();
		log.info("Received test message from SQS listener: {}", messagePayload);

		Mono.just(messagePayload)
			// 50%의 확률로 PASS하고, PASS시 Acknowledgement도 같이 보내도록 함. 나머지 50%는 에러 발생시켜서 메시지가 삭제되지 않도록 함
			.flatMap(payload -> {
				if (Math.random() < 0.5) {
					log.info("Test message processing passed for payload: {}", payload);
					return Mono.just(payload);
				} else {
					log.warn("Test message processing failed for payload: {}", payload);
					return Mono.error(new RuntimeException("Simulated processing error"));
				}
			})
			.then(Mono.defer(() -> Mono.fromFuture(acknowledgement.acknowledgeAsync())
				.then()))
			.onErrorResume(error -> {
				log.error("Error processing test message with payload {}: {}", messagePayload, error.getMessage());
				// 에러 발생 시 메시지가 삭제되지 않도록 함
				// 에러 메시지를 전송하고 에러 전파를 멈춘다 (에러 핸들링에서 에러가 발생하면 doOnError에서 잡음)
				return messageLogger.sendMessage(
					"Error processing test message with payload " + messagePayload + ": " + error.getMessage());
			})
			.doOnSuccess(_ -> log.info("Acknowledged test message with payload: {}", messagePayload))
			.doOnError(error -> log.error("Error processing test message with payload {}: {}", messagePayload,
				error.getMessage()))
			.subscribe();

	}
}
