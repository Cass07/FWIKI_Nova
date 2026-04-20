package wiki.feh.externalrestdemo.openai.batch.infra;

import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.core.MultipartField;
import com.openai.models.batches.Batch;
import com.openai.models.batches.BatchCreateParams;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FilePurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Qualifier("OpenAIBatchService")
public class OpenAIBatchService implements IBatchService {

    @Override
    public Mono<String> callRequestBatchApi(List<String> jsonlList) {
        String fileContent = String.join("\n", jsonlList);
        String fileName = "batch_input_" + System.currentTimeMillis() + ".jsonl";
        return uploadFile(fileContent, fileName)
                .flatMap(this::createBatch);
    }

    // batch file 업로드하고 id string을 리턴
    private Mono<String> uploadFile(String file, String fileName) {
        OpenAIClientAsync client = OpenAIOkHttpClientAsync.fromEnv();

        FileCreateParams fileParam = FileCreateParams.builder()
                .purpose(FilePurpose.BATCH)
                .file(MultipartField.<InputStream>builder()
                        .filename(fileName)
                        .value(new java.io.ByteArrayInputStream(file.getBytes()))
                        .build())
                .build();

        return Mono.fromFuture(client.files().create(fileParam))
                .flatMap(response -> {
                    log.info("Uploaded File ID: {}", response.id());
                    return Mono.just(response.id());
                });
    }

    // batch create 호출하고 batch id를 리턴
    private Mono<String> createBatch(String fileId) {
        OpenAIClientAsync client = OpenAIOkHttpClientAsync.fromEnv();

        BatchCreateParams batchParams = BatchCreateParams.builder()
                .inputFileId(fileId)
                .endpoint(BatchCreateParams.Endpoint.V1_RESPONSES)
                .completionWindow(BatchCreateParams.CompletionWindow._24H)
                .build();

        Mono<Batch> batchMono = Mono.fromFuture(client.batches().create(batchParams));

        return batchMono.flatMap(batch -> {
            log.info("Created Batch ID : {}", batch.id());
            return Mono.just(batch.id());
        });
    }
}
