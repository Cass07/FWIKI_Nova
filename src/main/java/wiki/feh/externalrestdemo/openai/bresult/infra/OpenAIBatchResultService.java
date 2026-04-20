package wiki.feh.externalrestdemo.openai.bresult.infra;

import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.core.http.HttpResponse;
import com.openai.models.batches.Batch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
@Qualifier("OpenAIBatchResultService")
public class OpenAIBatchResultService implements IBatchResultService {

    // Batch Retrieve 호출해서 결과 Id 리턴, 결과가 없으면 Mono.empty 리턴
    @Override
    public Mono<String> getBatchResultFileId(String batchId) {
        OpenAIClientAsync client = OpenAIOkHttpClientAsync.fromEnv();

        Mono<Batch> batchMono = Mono.fromFuture(client.batches().retrieve(batchId));

        return batchMono.flatMap(batch -> {
            if(batch.outputFileId().isEmpty()) {
                log.info("Batch ID : {} has no output file yet.", batch.id());
                return Mono.empty();
            }
            log.info("Retrieved Batch ID : {}, Status : {}", batch.id(), batch.status());
            return Mono.just(batch.outputFileId().get());
        });
    }

    // file id로부터 파일 읽어오기
    @Override
    public Mono<String> getFileContentById(String fileId) {
        OpenAIClientAsync client = OpenAIOkHttpClientAsync.fromEnv();

        Mono<HttpResponse> fileContentMono = Mono.fromFuture(client.files().content(fileId));

        return fileContentMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(httpResponse -> {
                    try (InputStream inputStream = httpResponse.body()) {
                        @SuppressWarnings("BlockingMethodInNonBlockingContext")
                        byte[] bytes = inputStream.readAllBytes();
                        return Mono.just(new String(bytes));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error reading file content: " + e.getMessage()));
                    }
                }).onErrorResume(error -> {
                    log.error("Error fetching file content", error);
                    return Mono.just("Error fetching file content: " + error.getMessage());
                });
    }

}
