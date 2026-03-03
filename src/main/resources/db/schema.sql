-- Hopzone Vote Verify — полная схема БД
-- После удаления таблиц: mysql -u root -p hopzone_verify < schema.sql

USE hopzone_verify;

DROP TABLE IF EXISTS vote_cases;

CREATE TABLE vote_cases
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_hash          VARCHAR(64)  NOT NULL UNIQUE,
    created_at_utc       DATETIME(6)  NOT NULL,
    ip                   VARCHAR(45)  NOT NULL,
    user_agent           VARCHAR(255) NULL,
    recaptcha_ok         BOOLEAN      NOT NULL,
    hopzone_http_status  INT          NULL,
    hopzone_raw_json     TEXT         NULL,
    hopzone_response_time_ms BIGINT   NULL,
    hopzone_status_code  INT          NULL,
    hopzone_apiver       VARCHAR(32)  NULL,
    voted                BOOLEAN      NOT NULL,
    vote_time            VARCHAR(64)  NULL,
    hopzone_server_time  VARCHAR(64)  NULL,
    screenshot_ip_path   VARCHAR(255) NOT NULL,
    screenshot_vote_path  VARCHAR(255) NOT NULL,
    note                 VARCHAR(255) NULL,
    hopzone_account_id   VARCHAR(64)  NULL,
    hopzone_server_id    VARCHAR(32)  NULL,
    user_contact         VARCHAR(512) NULL,
    game_nickname        VARCHAR(64)  NULL,
    telegram             VARCHAR(128) NULL,
    discord              VARCHAR(128) NULL,
    email                VARCHAR(255) NULL
);

CREATE INDEX idx_vote_cases_created_at ON vote_cases (created_at_utc);
CREATE INDEX idx_vote_cases_ip ON vote_cases (ip);
CREATE INDEX idx_vote_cases_voted ON vote_cases (voted);
