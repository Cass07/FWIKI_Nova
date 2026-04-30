package wiki.feh.externalrestdemo.openai.bresult.infra;

import reactor.util.function.Tuple2;
import wiki.feh.externalrestdemo.openai.bresult.dto.IApiResult;

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
     * @param resultType - 파싱 결과로 반환할 IApiResult 구현체 클래스 타입
     * @return
     */
    <T extends IApiResult> List<T> parseResultStringToApiResultList(String resultString, Class<T> resultType);
}
