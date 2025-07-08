package wiki.feh.externalrestdemo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import wiki.feh.externalrestdemo.util.config.R2dbcConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataR2dbcTest
@EnableAutoConfiguration
@ActiveProfiles("test")
@ContextConfiguration(classes = {R2dbcConfig.class, AsyncResultRepository.class})
class AsyncResultRepositoryTest {

    @Autowired
    private AsyncResultRepository asyncResultRepository;


    @DisplayName("r2dbc save")
    @Test
    void save() {
        // given
        AsyncResult asyncResult = new AsyncResult("test body");
        asyncResult.updateStatus(AsyncStatus.COMPLETED);

        // when
        AsyncResult expected = asyncResultRepository.save(asyncResult).block();

        // then
        assert expected != null;
        assertNotNull(expected.getCreatedAt());
        assertTrue(expected.getId() > 0);
    }

}