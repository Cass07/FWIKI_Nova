package wiki.feh.externalrestdemo.asyncapi.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AsyncResultRepository extends R2dbcRepository<AsyncResult, Integer> {
    Mono<AsyncResult> findById(int id);
    Mono<AsyncResult> findByIdAndFinishedAtIsNotNull(int id);
}
