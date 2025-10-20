DROP TABLE IF EXISTS async_result;

CREATE TABLE "async_result" (
    `id` INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `body` TEXT,
    `created_at` DATETIME NOT NULL,
    `finished_at` DATETIME,
    `stat` VARCHAR(20) DEFAULT 'PENDING'
);

CREATE TABLE `hero_quotes_list` (
                                    `idx` INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                    `id` VARCHAR(35) NOT NULL DEFAULT '0',
                                    `kind` VARCHAR(20) NOT NULL DEFAULT '0',
                                    `seq` INT(10) NOT NULL DEFAULT '0',
                                    `text` VARCHAR(3000) NOT NULL DEFAULT '0',
                                    `lang` CHAR(5) NOT NULL DEFAULT '0' 
);

CREATE TABLE `hero_quotes_list_kr` (
                                       `idx` INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                       `id` VARCHAR(35) NOT NULL DEFAULT '0',
                                       `kind` VARCHAR(20) NOT NULL DEFAULT '0',
                                       `seq` INT(10) NOT NULL DEFAULT '0',
                                       `text` VARCHAR(3000) NOT NULL DEFAULT '0',
                                       `editor` INT(10) NOT NULL DEFAULT '0' COMMENT 'google_id',
                                       `version` INT(10) NOT NULL,
                                       `is_visible` TINYINT(3) NOT NULL
);

CREATE TABLE `batch_info` (
                                       `idx` INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                       `batch_id` VARCHAR(35) NOT NULL DEFAULT '0',
                                       `stat` VARCHAR(20) NOT NULL DEFAULT '0',
                                       `created_at` DATETIME NOT NULL,
                                       `updated_at` DATETIME NOT NULL
);

CREATE TABLE `batch_quote_info` (
                              `idx` INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT,
                              `batch_info_id` INT(10) NOT NULL DEFAULT '0',
                              `stat` VARCHAR(20) NOT NULL DEFAULT '0',
                              `created_at` DATETIME NOT NULL,
                              `hero_id` VARCHAR(35) NOT NULL DEFAULT '0'
);

CREATE TABLE `batch_quote_result` (
                                    `idx` INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                    `batch_info_id` INT(10) NOT NULL DEFAULT '0',
                                    `hero_id` VARCHAR(35) NOT NULL DEFAULT '0',
                                    `body` TEXT,
                                    `created_at` DATETIME NOT NULL
);

CREATE TABLE `hero_list` (
    `id` VARCHAR(35) PRIMARY KEY NOT NULL,
    `name` VARCHAR(35) NOT NULL,
    `jpnamesub` VARCHAR(15) NOT NULL,
    `jpname` VARCHAR(10) NOT NULL,
    `korname` VARCHAR(10) NOT NULL,
    `kornamesub` VARCHAR(20) NOT NULL,
    `releasedate` DATETIME NOT NULL
)