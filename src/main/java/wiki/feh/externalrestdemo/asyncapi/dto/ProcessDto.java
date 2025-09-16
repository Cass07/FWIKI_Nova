package wiki.feh.externalrestdemo.asyncapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProcessDto {
    String result;
    String message;
    String resultBody;
}
