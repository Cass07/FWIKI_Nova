package wiki.feh.externalrestdemo.hero.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`hero_list`")
public class Hero {
    @Id
    @Column("`id`")
    private String id;

    @Column("korname")
    private String korName;
    @Column("kornamesub")
    private String korNameSub;
    @Column("jpname")
    private String jpName;
    @Column("jpnamesub")
    private String jpNameSub;
    @Column("`name`")
    private String enName;

    @Column("releasedate")
    private LocalDate releaseDate;
}
