package wiki.feh.externalrestdemo.openai.bresult.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
public class TestTableController {
    private final TestTableService testTableService;

    @GetMapping("/test/conn")
    public Mono<Void> testConnection() {
        return testTableService.decreaseValue2ById(1);

    }

}
