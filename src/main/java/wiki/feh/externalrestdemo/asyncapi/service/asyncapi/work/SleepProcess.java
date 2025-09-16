package wiki.feh.externalrestdemo.asyncapi.service.asyncapi.work;

import wiki.feh.externalrestdemo.asyncapi.domain.AsyncResult;
import wiki.feh.externalrestdemo.asyncapi.domain.AsyncStatus;
import wiki.feh.externalrestdemo.asyncapi.dto.OpenAPIRequestBody;

public class SleepProcess implements IProcess {

    private final int MAX_DELAY_TIME = 5000; // 최대 지연 시간 (5초)

    @Override
    public AsyncResult run(AsyncResult asyncResult, OpenAPIRequestBody openAPIRequestBody) throws Exception {
        int delayTime = Math.max(openAPIRequestBody.getDelay(), MAX_DELAY_TIME);

        // 메인 프로세스
        Thread.sleep(delayTime);

        // 작업 결과 갱신
        asyncResult.updateStatus(AsyncStatus.COMPLETED);
        asyncResult.updateBody(openAPIRequestBody.getResponseBody());

        return asyncResult;
    }
}
