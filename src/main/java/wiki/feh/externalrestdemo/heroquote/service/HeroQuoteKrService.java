package wiki.feh.externalrestdemo.heroquote.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteKr;
import wiki.feh.externalrestdemo.heroquote.domain.HeroQuoteKrRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class HeroQuoteKrService {
    private final HeroQuoteKrRepository heroQuoteKrRepository;

    // 특정 id, kind, seq에 해당하는 HeroQuoteKr의 최신 버전을 조회
    public Mono<Integer> findLatestVersionByIdKindSeq(String id, String kind, int seq) {
        return heroQuoteKrRepository.findFirstByIdAndKindAndSeqOrderByVersionDesc(id, kind, seq)
                .map(HeroQuoteKr::getVersion)
                .defaultIfEmpty(0)
                .onErrorReturn(0);
    }

    // HeroQuoteKr 리스트를 배치 저장
    public Mono<Void> batchSaveHeroQuoteKrList(List<HeroQuoteKr> heroQuoteKrList) {
        return heroQuoteKrRepository.batchSave(heroQuoteKrList);
    }
}
