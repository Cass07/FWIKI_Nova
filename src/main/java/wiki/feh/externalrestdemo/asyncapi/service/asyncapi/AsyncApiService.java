package wiki.feh.externalrestdemo.asyncapi.service.asyncapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import wiki.feh.externalrestdemo.asyncapi.domain.AsyncResult;
import wiki.feh.externalrestdemo.asyncapi.domain.AsyncResultRepository;
import wiki.feh.externalrestdemo.asyncapi.domain.AsyncStatus;
import wiki.feh.externalrestdemo.asyncapi.dto.OpenAPIRequestBody;
import wiki.feh.externalrestdemo.asyncapi.service.asyncapi.work.IProcess;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncApiService {

    private final AsyncResultRepository asyncResultRepository;

    @Transactional
    public Mono<AsyncResult> startAsyncApi(OpenAPIRequestBody requestBody, IProcess work) {

        AsyncResult asyncResult = new AsyncResult(requestBody.getResponseBody());

        // 객체를 저장하고
        return saveAsyncResult(asyncResult)
            .flatMap(saveTask -> {
                // 저장한 객체를 받아서 id를 가져오고
                int receivedId = saveTask.getId();
                log.info("Async task started with id: {}", receivedId);
                // 실제 프로세스는 Schedulers.boundedElastic를 쓴다
                processDataAsync(saveTask, requestBody, work)
                    .subscribeOn(Schedulers.boundedElastic())
                    // 결과를 받아서 업데이트하고
                    .flatMap(this::saveAsyncResult)
                    .doOnError(error -> {
                        // 작업 실패 시 에러를 저장
                        Mono.just(saveTask)
                            .map(asyncResult1 -> {
                                asyncResult1.updateBody("Error occurred: " + error.getMessage());
                                asyncResult1.updateStatus(AsyncStatus.FAILED);
                                return asyncResult1;
                            })
                            .flatMap(this::saveAsyncResult)
                            .subscribe();
                    })
                    .subscribe();

                // 작업 저장에 성공했으면 비동기적으로 작업을 시작하므로 조회를 위한 객체를 반환
                return Mono.just(asyncResult);
            });
    }

    @Transactional
    public Mono<AsyncResult> getAsyncApiResult(int id) {
        // 주어진 ID로 비동기 작업의 결과를 조회
        return asyncResultRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("No AsyncResult found for id: " + id)));
    }

    // asyncResult를 저장
    private Mono<AsyncResult> saveAsyncResult(AsyncResult asyncResult) {
        return asyncResultRepository.save(asyncResult)
            .doOnSuccess(saved -> log.info("AsyncResult saved with id: {}", saved.getId()))
            .doOnError(error -> {
                log.error("Error saving AsyncResult: {}", error.getMessage());
                throw new RuntimeException("Failed to save AsyncResult", error);
            });
    }

    // 비동기 작업의 시작과 끝을 기록하는 역할
    private Mono<AsyncResult> processDataAsync(AsyncResult asyncResult, OpenAPIRequestBody data, IProcess work) {
        // 작업 상태를 PROCESSING으로 업데이트
        return Mono.just(asyncResult)
                .map(asyncResult1 -> {
                    asyncResult1.updateStatus(AsyncStatus.RUNNING);
                    return asyncResult1;
                })
                .flatMap(this::saveAsyncResult)
                // 작업 수행
                .map(savedAsyncResult -> {
                    log.info("Starting processing for id: {}", savedAsyncResult.getId());

                    // 실제 작업을 객체한테 옮겨주기
                    try {
                        savedAsyncResult = work.run(savedAsyncResult, data);
                    } catch (Exception e) {
                        // 작업 중 예외 발생 시 작업 실패 처리
                        log.error("Error during processing: {}", e.getMessage());
                        savedAsyncResult.updateStatus(AsyncStatus.FAILED);
                        savedAsyncResult.updateBody("Error occurred: " + e.getMessage());
                        return savedAsyncResult;
                    }

                    log.info("Processing completed for id: {}", savedAsyncResult.getId());
                    return savedAsyncResult;
                });
    }
}
