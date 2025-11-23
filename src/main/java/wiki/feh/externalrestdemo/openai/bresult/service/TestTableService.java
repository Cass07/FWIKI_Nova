package wiki.feh.externalrestdemo.openai.bresult.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.openai.bresult.domain.TestTableRepository;
import wiki.feh.externalrestdemo.util.NamedLockManager;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestTableService {
    private final TestTableRepository testTableRepository;
    private final NamedLockManager namedLockManager;


    @Transactional
    public Mono<Void> decreaseValue2ById(int id) {
        String lockName = "test_table_" + id;

        return namedLockManager.executeWithNamedLock(lockName,
                testTableRepository.findById(id)
                        .flatMap(testTable -> {
                            testTable.decreaseValue2();
                            return testTableRepository.save(testTable);
                        })
                        .then()
        );
    }

}
