package wiki.feh.externalrestdemo.openai.bresult.infra;

import reactor.core.publisher.Mono;

public interface IBatchResultService {
    // Batch Retrieve 호출해서 결과 Id 리턴, 결과가 없으면 Mono.empty 리턴
    Mono<String> getBatchResultFileId(String batchId);

    // file id로부터 파일 읽어오기
    Mono<String> getFileContentById(String fileId);
}
