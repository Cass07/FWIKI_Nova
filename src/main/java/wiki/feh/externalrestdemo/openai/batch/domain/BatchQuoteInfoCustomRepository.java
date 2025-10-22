package wiki.feh.externalrestdemo.openai.batch.domain;

import reactor.core.publisher.Mono;

import java.util.List;

public interface BatchQuoteInfoCustomRepository {
    Mono<Void> batchSave(List<BatchQuoteInfo> batchQuoteInfoList);
}
