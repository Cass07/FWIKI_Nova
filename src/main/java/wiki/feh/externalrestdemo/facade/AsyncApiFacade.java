package wiki.feh.externalrestdemo.facade;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.dto.AsyncApiResponseBody;
import wiki.feh.externalrestdemo.dto.OpenAPIRequestBody;
import wiki.feh.externalrestdemo.service.asyncapi.AsyncApiService;

@RequiredArgsConstructor
@Component
public class AsyncApiFacade {
    private final AsyncApiService asyncApiService;

    public Mono<AsyncApiResponseBody> startAsyncApi(OpenAPIRequestBody requestBody) {
        // Endpoint URL 설정
        String endpoint = "/api/async";

        // 비동기 호출 결과를 responseBody에 매핑
        return asyncApiService.startAsyncApi(requestBody)
                .map(asyncResult -> new AsyncApiResponseBody(asyncResult, endpoint));
    }
}
