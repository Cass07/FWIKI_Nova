package wiki.feh.externalrestdemo.heroquote.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class HeroQuoteDto {

    /**
     * HeroQuote를 OpenAI Request 형식에 맞게 변환하기 위한 dto
     */
    @Getter
    @AllArgsConstructor
    public static class OpenAIBatchRequest {
        private String key;
        private int seq;
        private String text;

        public static OpenAIBatchRequest of (HeroQuote quote) {
            return new OpenAIBatchRequest(quote.getKind(), quote.getSeq(), quote.getText());
        }
    }


    /**
     * OpenAIBatchRequest list를 json string으로 변환하는 dto
     * 여기서 따로 할필요있나 그냥 ObjectMapper로 변환하면 될것같기도하고
     */
    @Getter
    @AllArgsConstructor
    public static class OpenAIBatchText {
        private String text;
        private static final ObjectMapper objectMapper = new ObjectMapper();

        public static OpenAIBatchText of (List<OpenAIBatchRequest> quotes){
            try {
                return new OpenAIBatchText(objectMapper.writeValueAsString(quotes));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
