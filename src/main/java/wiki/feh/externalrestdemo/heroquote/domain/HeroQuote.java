package wiki.feh.externalrestdemo.heroquote.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 원본 대사를 저장하는 엔티티
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`hero_quotes_list`")
public class HeroQuote {
    @Id
    @Column("idx")
    private int idx;

    @Column("id")
    private String id;

    @Column("kind")
    private String kind;

    @Column("seq")
    private int seq;

    @Column("text")
    private String text;

    @Column("lang")
    private QuoteLang lang;

}
