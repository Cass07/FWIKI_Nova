package wiki.feh.externalrestdemo.openai.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wiki.feh.externalrestdemo.openai.batch.domain.BatchInfo;

/**
 * @see <a href="https://platform.openai.com/docs/api-reference/batch">Batch API Reference</a>
 */

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class BatchDto {

    /**
     * @see <a href="https://platform.openai.com/docs/api-reference/batch/request-input">Batch Request Input Object</a>
     */
    @Getter
    @AllArgsConstructor
    public static class BatchRequestLine {
        private String custom_id;
        private String method;
        private String url;
        private BatchRequestLineBody body;

        /**
         * 대사 batch 요청 json line 생성
         * @param custom_id    hero id
         * @param model        모델명
         * @param input        HeroQuoteDto.OpenAIRequest json string
         * @param promptId     prompt id
         * @param outputTokens 최대 출력 토큰 수
         */
        public BatchRequestLine(String custom_id, String model, String input, String promptId, int outputTokens) {
            this.custom_id = custom_id;
            this.method = "POST";
            this.url = "/v1/responses";
            this.body = new BatchRequestLineBody(
                    model,
                    input,
                    new BatchRequestLineBodyPrompt(promptId),
                    new BatchRequestLineBodyText(new BatchRequestLineBodyTextFormat("text")),
                    outputTokens
            );
        }

    }

    @Getter
    @AllArgsConstructor
    public static class BatchRequestLineBody {
        private String model;
        private String input;
        private BatchRequestLineBodyPrompt prompt;
        private BatchRequestLineBodyText text;
        private int max_output_tokens;
    }

    @Getter
    @AllArgsConstructor
    public static class BatchRequestLineBodyPrompt {
        private String id;
    }

    @Getter
    @AllArgsConstructor
    public static class BatchRequestLineBodyText {
        private BatchRequestLineBodyTextFormat format;
    }

    @Getter
    @AllArgsConstructor
    public static class BatchRequestLineBodyTextFormat{
        private String type;
    }


    @Getter
    @AllArgsConstructor
    public static class BatchResponse {
        private String key;
        private int seq;
        private String result;
    }

    @Getter
    @AllArgsConstructor
    public static class BatchInfoDto {
        private int id;
        private String batchId;
        private String status;

        public static BatchInfoDto of (BatchInfo batchInfo) {
            return new BatchInfoDto(
                    batchInfo.getIdx(),
                    batchInfo.getBatchId(),
                    batchInfo.getStatus().name()
            );
        }
    }
}
