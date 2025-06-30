package wiki.feh.externalrestdemo.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AsyncResultRepository extends ReactiveCrudRepository<AsyncResult, Integer> {
    Mono<AsyncResult> findById(int id);
    Mono<AsyncResult> findByIdAndFinishedAtIsNotNull(int id);
}
