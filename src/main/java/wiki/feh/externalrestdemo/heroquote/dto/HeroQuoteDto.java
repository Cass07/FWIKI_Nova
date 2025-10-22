package wiki.feh.externalrestdemo.heroquote.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class HeroQuoteDto {

    /**
     * Batch Request의 content dto
     */
    @Getter
    @AllArgsConstructor
    public static class OpenAIBatchRequestContent {
        private String key;
        private int seq;
        private String text;

        public static OpenAIBatchRequestContent of (HeroQuote quote) {
            return new OpenAIBatchRequestContent(quote.getKind(), quote.getSeq(), quote.getText());
        }
    }

    /**
     * Batch Request의 chara dto
     */
    @Getter
    @AllArgsConstructor
    public static class OpenAIBatchRequestChara {
        private String chara_j;
        private String chara_k;

        public static OpenAIBatchRequestChara of (Hero hero) {
            return new OpenAIBatchRequestChara(hero.getJpName(), hero.getKorName());
        }

        public static OpenAIBatchRequestChara of (String jpName, String korName) {
            return new OpenAIBatchRequestChara(jpName, korName);
        }
    }

    /**
     * Batch Request json을 생성하기 위한 dto
     */
    @Getter
    @AllArgsConstructor
    public static class OpenAiBatchRequest {
        private List<OpenAIBatchRequestChara> chara;
        private List<OpenAIBatchRequestContent> content;

        public static OpenAiBatchRequest of (Hero hero, List<HeroQuote> quotes) {
            return new OpenAiBatchRequest(
                    List.of(OpenAIBatchRequestChara.of(hero)),
                    quotes.stream().map(OpenAIBatchRequestContent::of).toList()
            );
        }

        public static OpenAiBatchRequest of (List<Hero> heroes, List<HeroQuote> quotes) {
            return new OpenAiBatchRequest(
                    heroes.stream().map(OpenAIBatchRequestChara::of).toList(),
                    quotes.stream().map(OpenAIBatchRequestContent::of).toList()
            );
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

        public static OpenAIBatchText of (OpenAiBatchRequest dto){
            try {
                return new OpenAIBatchText(objectMapper.writeValueAsString(dto));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
