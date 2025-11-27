package wiki.feh.externalrestdemo.util;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Repository
public class NamedLockManager {
    private final DatabaseClient databaseClient;
    private final ConnectionFactory connectionFactory;

    public Mono<Void> executeWithNamedLock(String lockName, Mono<Void> operation) {
        return Mono.usingWhen(
                Mono.from(connectionFactory.create()),
                conn -> acquireLock(lockName, conn).then(operation),
                conn -> releaseLock(lockName, conn)
                        .onErrorResume(err -> {
                            log.error("Error releasing lock, but closing connection anyway", err);
                            return Mono.empty();
                        })
                        .then(Mono.from(conn.close()))
                        .doOnSuccess(_ -> log.info("Connection closed: {}", conn)),
                (conn, err) -> Mono.from(conn.close())
                        .doOnSuccess(_ -> log.error("Connection closed after error: {}", conn, err)),
                conn -> Mono.from(conn.close())
                        .doOnSuccess(_ -> log.info("Connection closed on cancel: {}", conn))
        );
    }

    private Mono<Void> acquireLock(String lockName, Connection connection) {
        // connection을 받아서 직접 쿼리 execute
        return Mono.from(connection.createStatement("SELECT GET_LOCK(?, 10) AS lock_status")
                .bind(0, lockName)
                .execute())
                .doOnNext(_ -> log.info("Acquiring lock: {}", lockName))
                // 결과 매핑
                .flatMap(result -> Mono.from(result.map((row, _) -> row.get("lock_status", Integer.class))))
                // 결과로부터 상태 확인하여 처리
                .flatMap(status -> {
                    if (status != null && status == 1) {
                        log.info("Lock acquired: {}", lockName);
                        return Mono.empty(); // Lock acquired
                    } else {
                        log.error("Failed to acquire lock: {}, status : {}", lockName, status);
                        return Mono.error(new RuntimeException("Failed to acquire lock: " + lockName));
                    }
                });
    }

    // 락을 획득하는 메소드, 획득에 실패하면 runtime exception 발생
    private Mono<Void> acquireLock(String lockName) {
        return databaseClient.sql("SELECT GET_LOCK(?, 10) AS lock_status")
                .bind(0, lockName)
                .map(row -> row.get("lock_status", Integer.class))
                .first()
                .flatMap(status -> {
                    if (status != null && status == 1) {
                        return Mono.empty(); // Lock acquired
                    } else {
                        log.error("Failed to acquire lock: {}, status : {}", lockName, status);
                        return Mono.error(new RuntimeException("Failed to acquire lock: " + lockName));
                    }
                });
    }

    private Mono<Void> releaseLock(String lockName, Connection connection) {
        // connection을 받아서 직접 쿼리 execute
        return Mono.from(connection.createStatement("SELECT RELEASE_LOCK(?) AS release_status")
                .bind(0, lockName)
                .execute())
                // 결과 매핑
                .flatMap(result -> Mono.from(result.map((row, _) -> row.get("release_status", Integer.class))))
                // 결과로부터 상태 확인하여 처리
                .flatMap(status -> {
                    if (status != null && status == 1) {
                        log.info("Lock released: {}", lockName);
                        return Mono.empty(); // Lock released
                    } else {
                        log.error("Failed to release lock: {}, status : {}", lockName, status);
                        return Mono.error(new RuntimeException("Failed to release lock: " + lockName));
                    }
                });
    }

    // 락을 릴리즈하는 메소드, 실패하면 Runtime Exception 발생
    private Mono<Void> releaseLock(String lockName) {
        return databaseClient.sql("SELECT RELEASE_LOCK(?) AS release_status")
                .bind(0, lockName)
                .map(row -> row.get("release_status", Integer.class))
                .first()
                .flatMap(status -> {
                    if (status != null && status == 1) {
                        return Mono.empty(); // Lock released
                    } else {
                        log.error("Failed to release lock: {}, status : {}", lockName, status);
                        return Mono.error(new RuntimeException("Failed to release lock: " + lockName));
                    }
                });
    }
}
