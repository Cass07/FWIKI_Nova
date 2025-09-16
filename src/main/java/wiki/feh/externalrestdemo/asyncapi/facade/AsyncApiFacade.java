package wiki.feh.externalrestdemo.asyncapi.facade;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.asyncapi.dto.AsyncApiResponseBody;
import wiki.feh.externalrestdemo.asyncapi.dto.AsyncApiResponseGetBody;
import wiki.feh.externalrestdemo.asyncapi.dto.OpenAPIRequestBody;
import wiki.feh.externalrestdemo.asyncapi.service.asyncapi.AsyncApiService;
import wiki.feh.externalrestdemo.asyncapi.service.asyncapi.work.SleepProcess;

@RequiredArgsConstructor
@Component
public class AsyncApiFacade {
    private final AsyncApiService asyncApiService;

    public Mono<AsyncApiResponseBody> startAsyncApi(OpenAPIRequestBody requestBody) {
        // Endpoint URL 설정
        String endpoint = "/api/async";

        // 비동기 호출 결과를 responseBody에 매핑
        return asyncApiService.startAsyncApi(requestBody, new SleepProcess())
                .map(asyncResult -> new AsyncApiResponseBody(asyncResult, endpoint));
    }

    public Mono<AsyncApiResponseGetBody> getAsyncApiResult(int id) {
        // 주어진 ID로 비동기 작업의 결과를 조회
        return asyncApiService.getAsyncApiResult(id)
                .map(asyncResult -> new AsyncApiResponseGetBody(asyncResult.getStat(), asyncResult.getBody()));
    }
}
