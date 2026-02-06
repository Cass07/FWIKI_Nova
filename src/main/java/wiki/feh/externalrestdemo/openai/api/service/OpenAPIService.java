package wiki.feh.externalrestdemo.openai.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.core.MultipartField;
import com.openai.core.http.HttpResponse;
import com.openai.models.batches.Batch;
import com.openai.models.batches.BatchCreateParams;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FilePurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchQuoteInfo;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchStatus;
import wiki.feh.externalrestdemo.openai.batch.service.BatchQuoteInfoService;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;

import java.io.InputStream;
import java.util.List;

/**
 * API를 제외한 나머지 로직 개발을 위해서 우선 모킹 서비스를 구현
 * API Result json을 가공해서 넘겨주는 책임은 여기서 지도록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAPIService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BatchQuoteInfoService batchQuoteInfoService;

    // jsonL list를 받아서 batch 파일을 업로드하고 batch create를 호출한 후 id를 리턴함
    public Mono<String> callRequestBatchApi(List<String> jsonlList) {
        String fileContent = String.join("\n", jsonlList);
        String fileName = "batch_input_" + System.currentTimeMillis() + ".jsonl";
        return uploadFile(fileContent, fileName)
                .flatMap(this::createBatch);
    }

    // batch file 업로드하고 id string을 리턴
    public Mono<String> uploadFile(String file, String fileName) {
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
    public Mono<String> createBatch(String fileId) {
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

    // Batch Retrieve 호출해서 결과 Id 리턴, 결과가 없으면 Mono.empty 리턴
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


    // heroId/jsonResult(String)와 batchId를 받아서 해당 batchId로 저장된 BatchQuoteInfo 객체 중 heroId가 일치하고 상태가 Pending인 BatchQuote와 jsonResult 튜플을 리턴
    private Flux<Tuple2<BatchQuoteInfo, String>> matchResponseAndBatchQuoteInfo(List<Tuple2<String, String>> responseBody, int batchId) {
        return batchQuoteInfoService.findBatchQuoteInfoByBatchInfoId(batchId)
                // 검색 성능을 위해서 MAP으로 변환하고
                .collectMap(BatchQuoteInfo::getHeroId, bqi -> bqi)
                //responseBody list를 돌면서 매칭하고 없으면 null 반환한 다음에, mapNotNull로 걸러내도록함
                .flatMapMany(batchQuoteInfoMap -> Flux.fromIterable(responseBody)
                        .mapNotNull(TupleUtils.function((heroId, jsonResult) -> {
                            BatchQuoteInfo bqi = batchQuoteInfoMap.get(heroId);
                            if (bqi == null || !bqi.getStatus().equals(BatchStatus.PENDING)) {
                                log.warn("No BatchQuoteInfo found for heroId: {} in batchId: {}", heroId, batchId);
                                return null;
                            } else {
                                log.debug("Matched BatchQuoteInfo for heroId: {}", heroId);
                                return Tuples.of(bqi, jsonResult);
                            }
                        })));
    }

    // hero id, json result string을 받아서 (본문 전체말고 결과 스트링만), hero id/apiResult list tuple list로 반환할것
    public Flux<Tuple2<BatchQuoteInfo, List<BResultDto.ApiResult>>> receiveResultMock(List<Tuple2<String, String>> responseBody, int batchId) {
        return matchResponseAndBatchQuoteInfo(responseBody, batchId)
                .map(TupleUtils.function((batchQuoteInfo, jsonResult) -> {
                    String heroId = batchQuoteInfo.getHeroId();
                    log.debug("Processing result for heroId: {}", heroId);
                    log.debug("JSON Result: {}", jsonResult);
                    // json string을 파싱해서 ApiResult 리스트로 변환.
                    try {
                        List<BResultDto.ApiResult> apiResults = objectMapper.readValue(
                                jsonResult, new TypeReference<>() {});
                        return Tuples.of(batchQuoteInfo, apiResults);
                    } catch (Exception e) {
                        // parsing에 실패하면 일단 빈 리스트를 반환.
                        log.error("Error parsing JSON result for heroId {}: {}", heroId, e.getMessage());
                        return Tuples.of(batchQuoteInfo, java.util.Collections.<BResultDto.ApiResult>emptyList());
                    }
                }))
                .doOnNext(_ -> log.info("Mock OpenAI API result processed."));
    }
}
