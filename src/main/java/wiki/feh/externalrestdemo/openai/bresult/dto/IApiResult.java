package wiki.feh.externalrestdemo.openai.bresult.dto;

import wiki.feh.externalrestdemo.heroquotekr.domain.HeroQuoteKr;

/**
 * API 파싱 결과를 추상화한 인터페이스
 * 구현체가 변경되어도 의존 코드에 영향 없음
 */
public interface IApiResult {
    HeroQuoteKr toHeroQuoteKr(String heroId, int version, int status);
}

