package wiki.feh.externalrestdemo.asyncapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import wiki.feh.externalrestdemo.asyncapi.domain.AsyncResult;

@AllArgsConstructor
@Builder
@Getter
public class AsyncApiResponseBody {
    private int id;
    private String endpoint;

    public AsyncApiResponseBody(AsyncResult asyncResult, String endpoint) {
        this.id = asyncResult.getId();
        this.endpoint = endpoint + "/" + this.id;
    }
}
