package wiki.feh.externalrestdemo.heroquote.domain;


import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * 테이블의 string을 읽어오면서 QouteLang enum으로 변환하는 converter
 */
@ReadingConverter
public class QuoteLangConverter implements Converter<String, QuoteLang> {
    @Override
    public QuoteLang convert(@NonNull String source) {
        return QuoteLang.fromCode(source);
    }
}
