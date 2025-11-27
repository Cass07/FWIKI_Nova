package wiki.feh.externalrestdemo.openai.bresult.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class BResultDto {

    @Getter
    @AllArgsConstructor
    public static class ApiResult {
        private String key;
        private int seq;
        private String result;
    }
}
