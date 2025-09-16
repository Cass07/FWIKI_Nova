package wiki.feh.externalrestdemo.asyncapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class OpenAPIRequestBody {
    private String responseBody;
    private int delay;
    private int responseCode;
}
