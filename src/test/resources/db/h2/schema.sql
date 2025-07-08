DROP TABLE IF EXISTS async_result;

CREATE TABLE "async_result" (
    `id` INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `body` TEXT,
    `created_at` DATETIME NOT NULL,
    `finished_at` DATETIME,
    `stat` VARCHAR(20) DEFAULT 'PENDING'
)