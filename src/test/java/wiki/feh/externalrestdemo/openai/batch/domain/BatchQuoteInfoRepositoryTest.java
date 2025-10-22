package wiki.feh.externalrestdemo.openai.batch.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import wiki.feh.externalrestdemo.util.config.R2dbcConfig;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@EnableAutoConfiguration
@ActiveProfiles("test")
@ContextConfiguration(classes = {BatchQuoteInfoRepository.class, R2dbcConfig.class, BatchQuoteInfoCustomRepository.class})
class BatchQuoteInfoRepositoryTest {

    @Autowired
    private BatchQuoteInfoRepository batchQuoteInfoRepository;

    @DisplayName("findByIdx Test")
    @Test
    void findByIdx() {
        // given
        int idx = 1;

        // when
        var maybeBatchQuoteInfo = batchQuoteInfoRepository.findByIdx(idx).block();

        // then
        assertNotNull(maybeBatchQuoteInfo);
    }

    @DisplayName("Batch Save Test")
    @Test
    void batchSave() {
        // given
        LocalDateTime now = LocalDateTime.now();

        int batchInfoId = 2;
        String heroId_1 = "hero_123";
        String heroId_2 = "hero_456";

        BatchQuoteInfo info1 = BatchQuoteInfo.builder()
                .batchInfoId(batchInfoId)
                .status(BatchStatus.REQUESTED)
                .createdAt(now)
                .heroId(heroId_1)
                .build();

        BatchQuoteInfo info2 = BatchQuoteInfo.builder()
                .batchInfoId(batchInfoId)
                .status(BatchStatus.REQUESTED)
                .createdAt(now)
                .heroId(heroId_2)
                .build();

        // when
        batchQuoteInfoRepository.batchSave(java.util.List.of(info1, info2)).block();

        var savedInfos = batchQuoteInfoRepository.findByBatchInfoId(batchInfoId).collectList().block();

        // then

        System.out.println("Saved Infos: " + savedInfos);
        assertNotNull(savedInfos);
        assertEquals(2, savedInfos.size());
        assertTrue(savedInfos.stream().allMatch(info -> info.getIdx() > 0));
    }

}