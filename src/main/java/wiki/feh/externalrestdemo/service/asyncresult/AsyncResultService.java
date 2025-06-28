package wiki.feh.externalrestdemo.service.asyncresult;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.domain.AsyncResult;

import java.time.LocalDateTime;

@Service
public class AsyncResultService {

    public Mono<AsyncResult> addResult(AsyncResult result) {
        // business logic

        return Mono.just(result);
    }

    public Mono<AsyncResult> updateResult(AsyncResult result) {
        //business logic

        return Mono.just(result);
    }

    public Mono<AsyncResult> findResultById(int id) {

        //return stub data
        return Mono.just(new AsyncResult(id, "body", LocalDateTime.now(), LocalDateTime.now()));
    }
}
