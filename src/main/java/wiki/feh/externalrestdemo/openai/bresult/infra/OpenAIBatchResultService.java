package wiki.feh.externalrestdemo.openai.bresult.infra;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.core.http.HttpResponse;
import com.openai.models.batches.Batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import wiki.feh.externalrestdemo.openai.bresult.infra.exception.BatchResultFileNotExistException;

@Slf4j
@Component
@RequiredArgsConstructor
@Qualifier("OpenAIBatchResultService")
public class OpenAIBatchResultService implements IBatchResultService {

    // Batch Retrieve 호출해서 결과 Id 리턴, 결과가 없으면 Mono.empty 리턴
    private Mono<String> getBatchResultFileId(String batchId) {
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
    private Mono<String> getFileContentById(String fileId) {
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
                        return Mono.error(new BatchResultFileNotExistException("Error reading file content: " + e.getMessage()));
                    }
                });
    }

    // batchId를 받아서 결과 파일 ID 조회 -> 파일 내용 조회 -> 줄 단위로 분리해서 list로 묶어서 반환
    // 외부 API에 따라 로직이 달라지므로 이 쪽에 분해해서 넣음
    @Override
    public Mono<java.util.List<String>> getJsonListFromBatchId(String batchId) {
        return this.getBatchResultFileId(batchId)
                .switchIfEmpty(Mono.error(new RuntimeException("No result file ID found for batchId: " + batchId)))
                // 결과 파일 ID로 파일 내용 조회
                .flatMap(fileId -> {
                    log.info("Retrieved output file ID: {}", fileId);
                    return this.getFileContentById(fileId);
                })
                // 파일 내용을 줄 단위로 분리해서 list로 묶음
                .flatMapMany(fileContent -> Flux.fromArray(fileContent.split("\n")))
                .collectList();
    }

}
