package wiki.feh.externalrestdemo.openai.bresult.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import wiki.feh.externalrestdemo.openai.bresult.dto.IApiResult;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Qualifier("BatchResultJsonParseV1")
public class BatchResultJsonParseV1 implements IBatchResultJsonParse {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T extends IApiResult> List<T> parseResultStringToApiResultList(String resultString, Class<T> resultType) {
        try {
            if(resultString == null || resultString.isEmpty()) {
                log.error("Result string is null or empty");
                return null;
            }

            List<T> apiResultList = objectMapper.readValue(
                    resultString,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, resultType)
            );

            if (apiResultList == null || apiResultList.isEmpty()) {
                log.error("Parsed ApiResult list is null or empty: {}", resultString);
                return null;
            }

            return apiResultList;
        } catch (Exception e) {
            log.error("Failed to parse result string to ApiResult list: {}", resultString, e);
        }

        return null;
    }

    @Override
    public Tuple2<String, String> parseResponseJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String heroId;
            String result = "";
            if (root.has("custom_id")) {
                heroId = root.get("custom_id").asText();
            } else {
                log.error("json missing hero_id field: {}", json);
                return null;
            }
            // - `response -> body -> output list -> type = message, status = completed인 객체의 content list (1개만 있음)-> text`

            JsonNode currentNode;

            if (root.has("response")) {
                currentNode = root.get("response");
            } else {
                log.error("json missing response field: {}", json);
                return null;
            }

            if (currentNode.has("body")) {
                currentNode = currentNode.get("body");
            } else {
                log.error("json missing body field: {}", json);
                return null;
            }

            if (currentNode.has("output")) {
                currentNode = currentNode.get("output");
            } else {
                log.error("json missing output field: {}", json);
                return null;
            }

            boolean isFound = false;
            for (JsonNode outputItem : currentNode) {
                if (outputItem.has("type") && outputItem.get("type").asText().equals("message")
                        && outputItem.has("status") && outputItem.get("status").asText().equals("completed")) {

                    // type :message, status:completed인 항목에서 content list를 조회
                    currentNode = outputItem.get("content");
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                log.error("json missing completed message output item: {}", json);
                return null;
            }

            isFound = false;
            for (JsonNode contentItem : currentNode) {
                if (contentItem.has("text")) {
                    result = contentItem.get("text").asText();
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                log.error("json missing text field in content: {}", json);
                return null;
            }

            return Tuples.of(heroId, result);
        } catch (Exception e) {
            log.error("Failed to parse JSONL string: {}", json, e);
            return null;
        }
    }
}
