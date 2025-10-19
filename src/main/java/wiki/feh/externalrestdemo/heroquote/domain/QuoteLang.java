package wiki.feh.externalrestdemo.heroquote.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuoteLang {
    JP("jp"),
    EN("en");

    private final String code;

    public static QuoteLang fromCode(String code) {
        for (QuoteLang lang : QuoteLang.values()) {
            if (lang.getCode().equalsIgnoreCase(code)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unknown language code: " + code);
    }
}
