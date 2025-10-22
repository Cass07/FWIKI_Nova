package wiki.feh.externalrestdemo.openai.batch.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchInfoRepository extends R2dbcRepository<BatchInfo, Integer> {

}
