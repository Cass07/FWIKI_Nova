package wiki.feh.externalrestdemo.heroquote.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import wiki.feh.externalrestdemo.openai.bresult.dto.BResultDto;

/**
 * 대사 번역을 저장하는 엔티티
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`hero_quotes_list_kr`")
public class HeroQuoteKr {
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

    @Column("editor")
    private int editorId;

    @Column("version")
    private int version;

    /**
     * 0: 미검수, 1: 검수완료 공개, 2: 검수완료 비공개
     * 변수명 직관적으로 변경
     */
    @Column("is_visible")
    private int status;

    public HeroQuoteKr(BResultDto.ApiResult response, String heroId, int version, int status) {
        this.id = heroId;
        this.kind = response.getKey();
        this.seq = response.getSeq();
        this.text = response.getResult();
        // 기계번역의 editor Id는 65
        this.editorId = 65;
        this.version = version;
        this.status = status;
    }


}
