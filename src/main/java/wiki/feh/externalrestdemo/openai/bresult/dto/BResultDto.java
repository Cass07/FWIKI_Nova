package wiki.feh.externalrestdemo.openai.bresult.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class BResultDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResult {
        private String key;
        private int seq;
        private String result;

        @Override
        public String toString() {
            return String.format("ApiResult(key=%s, seq=%d, result=%s)", key, seq, result);
        }
    }
}
