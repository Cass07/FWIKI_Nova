package wiki.feh.externalrestdemo.service.asyncapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import wiki.feh.externalrestdemo.domain.AsyncResult;
import wiki.feh.externalrestdemo.domain.AsyncResultRepository;
import wiki.feh.externalrestdemo.dto.OpenAPIRequestBody;
import wiki.feh.externalrestdemo.dto.ProcessDto;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncApiService {

    private final AsyncResultRepository asyncResultRepository;

    public Mono<AsyncResult> startAsyncApi(OpenAPIRequestBody requestBody) {
        // 임시로 id 생성시킴 (나중에 DB에 저장하는 로직 추가 예정)
        //int id = (int) (Math.random() * Integer.MAX_VALUE);

        AsyncResult asyncResult = new AsyncResult(requestBody.getResponseBody());

        return Mono.fromCallable(() -> {
                // 먼저 asyncResult를 저장해서 id를 할당받기
                // 블록킹 호출로 저장
                return asyncResultRepository.save(asyncResult).block();
            })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(saveTask -> {
                int receivedId = saveTask.getId();
                log.info("Async task started with id: {}", receivedId);
                processDataAsync(receivedId, requestBody)
                        .doOnNext(result -> log.info("Processing finished: {} {}", result.getMessage(), receivedId))
                        .doOnSuccess(result -> {
                            // 작업 성공하면 테이블에 결과와 성공 여부 저장
                            updateTaskResult(receivedId, result);
                        })
                        .doOnError(error -> {
                            // 작업 실패 시 에러를 저장
                            updateTaskError(receivedId, error);
                        })
                        .subscribe();
                return Mono.just(asyncResult);
            });
    }

    private Mono<ProcessDto> processDataAsync(int id, OpenAPIRequestBody data) {
        // 작업 상태를 PROCESSING으로 업데이트
        updateTaskStatus(id, "PROCESSING");

        return Mono.fromCallable(() -> {
            // 시간이 오래 걸리는 작업 수행
            log.info("Processing data for id: {}", id);
            Thread.sleep(5000); // 예시용 딜레이
            return new ProcessDto("completed", "작업 완료됨", data.getResponseBody());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void updateTaskResult(int id, ProcessDto result) {
        // 작업 결과를 DB에 저장하는 로직 (나중에 구현 예정)
        // 예: asyncResultRepository.updateResult(id, result);
        log.info("Task {} completed with result: {}", id, result);
    }

    private void updateTaskError(int id, Throwable error) {
        // 작업 실패 시 에러를 DB에 저장하는 로직 (나중에 구현 예정)
        // 예: asyncResultRepository.updateError(id, error);
        log.error("Task {} failed with error: {}", id, error.getMessage());
    }

    private void updateTaskStatus(int id, String status) {
        // 작업 상태를 DB에 업데이트하는 로직 (나중에 구현 예정)
        // 예: asyncResultRepository.updateStatus(id, status);
        log.info("Task {} status updated to: {}", id, status);
    }
}
