INSERT INTO "async_result" (body, created_at, finished_at, stat)
VALUES ('테스트 작업', '2024-12-26 12:00:00', '2024-12-26 12:05:00', 'PENDING');

INSERT INTO `hero_quotes_list` (`id`, `kind`, `seq`, `text`, `lang`)
VALUES
    ('test', 'Join', '1', 'test join jp', 'jp');

INSERT INTO `batch_info` (`batch_id`, `stat`, `created_at`, `updated_at`)
VALUES
    ('batch_001', 'IN_PROGRESS', '2024-12-26 12:00:00', '2024-12-26 12:10:00');


INSERT INTO `batch_quote_info` (`batch_info_id`, `stat`, `created_at`, `hero_id`)
VALUES
    (1, 'PENDING', '2024-12-26 12:00:00', 'hero_001');

INSERT INTO `hero_quotes_list_kr` (`idx`, `id`, `kind`, `seq`, `text`, `editor`, `version`, `is_visible`)
VALUES
    (1, 'hero_test', 'JOIN', 1, '테스트 조인', 1, 1, 1),
    (2, 'hero_test', 'JOIN', 1, '테스트 조인 version 2', 1, 2, 1);

INSERT INTO `hero_list` (`id`, `name`, `jpnamesub`, `jpname`, `korname`, `kornamesub`, `releasedate`)
VALUES
    ('hero_001', 'Hero: One', 'ヒーロー1 sub', 'ヒーロー1', '히어로1', '히어로1 sub', '2024-01-01'),
    ('hero_002', 'Hero: Two', 'ヒーロー2 sub', 'ヒーロー2', '히어로2', '히어로2 sub', '2024-02-01');

INSERT INTO `test_table` (`id`, `value_1`, `value_2`)
VALUES
    (1, 'Value 1A', 0);