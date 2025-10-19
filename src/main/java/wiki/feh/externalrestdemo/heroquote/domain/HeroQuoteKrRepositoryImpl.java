package wiki.feh.externalrestdemo.heroquote.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class HeroQuoteKrRepositoryImpl implements HeroQuoteKrCustomRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Void> batchSave(List<HeroQuoteKr> heroQuoteKrList) {
        return databaseClient.inConnection(conn ->
                Mono.from(conn.createStatement("INSERT INTO hero_quotes_list_kr (id, kind, seq, `text`, editor, `version`, is_visible) VALUES " +
                        String.join(", ",
                                heroQuoteKrList.stream()
                                        .map(this::heroQuoteKrToValueString)
                                        .toList()
                        )
                )
                .execute())
                .then());
    }

    private String heroQuoteKrToValueString(HeroQuoteKr hq) {
        return String.format("('%s', '%s', %d, '%s', %d, %d, %d)",
                hq.getId(),
                hq.getKind(),
                hq.getSeq(),
                hq.getText().replace("'", "''"), // 작은따옴표 이스케이프 처리
                hq.getEditorId(),
                hq.getVersion(),
                hq.getStatus()
        );
    }
}
