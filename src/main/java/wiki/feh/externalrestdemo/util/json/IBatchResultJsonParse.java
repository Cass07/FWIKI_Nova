package wiki.feh.externalrestdemo.util.json;

import reactor.util.function.Tuple2;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;

import java.util.List;

public interface IBatchResultJsonParse {
    /**
     * Batch API response json string을 파싱해서 hero id, result string tuple로 반환
     * @param json
     * @return
     */
    Tuple2<String, String> parseResponseJson(String json);

    /**
     * AI Model 결과 string을 파싱해서 BResultDto.ApiResult 리스트로 반환
     * @param resultString
     * @return
     */
    List<BResultDto.ApiResult> parseResultStringToApiResultList(String resultString);
}
