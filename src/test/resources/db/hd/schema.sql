DROP TABLE IF EXISTS "async_result";

CREATE TABLE "async_result" (
    `id` BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    `name` TEXT,
    `created_at` DATETIME NOT NULL,
    `finished_at` DATETIME,
    `status` VARCHAR(20) DEFAULT 'PENDING'
)