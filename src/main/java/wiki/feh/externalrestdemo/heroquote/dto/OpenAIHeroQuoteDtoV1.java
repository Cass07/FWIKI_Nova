package wiki.feh.externalrestdemo.heroquote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wiki.feh.externalrestdemo.hero.domain.Hero;
import wiki.feh.externalrestdemo.heroquote.agg.HeroQuoteAgg;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuote;

import java.util.List;

/**
 * HQ to Json Converter 역할을 수행하는 DTO????
 */
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class OpenAIHeroQuoteDtoV1 {

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

        public static OpenAiBatchRequest of (Hero hero, HeroQuoteAgg heroQuoteAgg) {
            return new OpenAiBatchRequest(
                    List.of(OpenAIBatchRequestChara.of(hero)),
                    heroQuoteAgg.getHeroQuotes().stream().map(OpenAIBatchRequestContent::of).toList()
            );
        }

    }
}
