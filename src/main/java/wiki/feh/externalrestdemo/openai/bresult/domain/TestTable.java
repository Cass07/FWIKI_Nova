package wiki.feh.externalrestdemo.openai.bresult.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@AllArgsConstructor
@Table(name = "test_table")
public class TestTable {
    @Id
    @Column("id")
    private int id;

    @Column("value_1")
    private String value1;

    @Column("value_2")
    private int value2;


    public void decreaseValue2() {
        this.value2 -= 1;
    }
}
