package wiki.feh.externalrestdemo.openai.bresult.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wiki.feh.externalrestdemo.heroquotekr.domain.HeroQuoteKr;

/**
 * IApiResult의 V1 구현체
 * 기본 필드만 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResultV1 implements IApiResult {
    private String key;
    private int seq;
    private String result;

    @Override
    public String toString() {
        return String.format("ApiResultV1(key=%s, seq=%d, result=%s)", key, seq, result);
    }

    @Override
    public HeroQuoteKr toHeroQuoteKr(String heroId, int version, int status)
    {
        return HeroQuoteKr.builder()
                .id(heroId)
                .kind(this.key)
                .seq(this.seq)
                .text(this.result)
                .editorId(65) // 기계번역의 editor Id는 65
                .version(version)
                .status(status)
                .build();
    }
}

