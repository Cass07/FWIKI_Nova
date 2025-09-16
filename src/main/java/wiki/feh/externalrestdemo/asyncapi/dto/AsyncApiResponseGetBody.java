package wiki.feh.externalrestdemo.asyncapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wiki.feh.externalrestdemo.asyncapi.domain.AsyncStatus;


@AllArgsConstructor
@Getter
public class AsyncApiResponseGetBody {
    private AsyncStatus status;
    private String body;
}
