package wiki.feh.externalrestdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wiki.feh.externalrestdemo.domain.AsyncStatus;


@AllArgsConstructor
@Getter
public class AsyncApiResponseGetBody {
    private AsyncStatus status;
    private String body;
}
