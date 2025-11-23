package wiki.feh.externalrestdemo.openai.bresult.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestTableRepository extends R2dbcRepository<TestTable, Integer> {
}
