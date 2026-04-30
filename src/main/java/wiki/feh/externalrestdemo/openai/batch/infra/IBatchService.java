package wiki.feh.externalrestdemo.openai.batch.infra;

import reactor.core.publisher.Mono;

import java.util.List;

public interface IBatchService {
    // jsonL list를 받아서 batch 파일을 업로드하고 batch create를 호출한 후 id를 리턴하는 메소드
    Mono<String> callRequestBatchApi(List<String> jsonlList);
}
