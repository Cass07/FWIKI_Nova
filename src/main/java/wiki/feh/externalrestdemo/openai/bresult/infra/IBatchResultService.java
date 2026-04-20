package wiki.feh.externalrestdemo.openai.bresult.infra;

import reactor.core.publisher.Mono;

import java.util.List;

public interface IBatchResultService {
    Mono<List<String>> getJsonListFromBatchId(String batchId);
}
