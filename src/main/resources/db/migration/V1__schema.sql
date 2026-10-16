CREATE TABLE province (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    updated_at  DATETIME(6)  NULL,
    updated_by  VARCHAR(100) NULL,
    deleted_at  DATETIME(6)  NULL,
    deleted_by  VARCHAR(100) NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    active_key  TINYINT GENERATED ALWAYS AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    UNIQUE KEY uk_province_name (name, active_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE branch (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    province_id BIGINT       NOT NULL,
    code        VARCHAR(30)  NOT NULL,
    name        VARCHAR(150) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    updated_at  DATETIME(6)  NULL,
    updated_by  VARCHAR(100) NULL,
    deleted_at  DATETIME(6)  NULL,
    deleted_by  VARCHAR(100) NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    active_key  TINYINT GENERATED ALWAYS AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    UNIQUE KEY uk_branch_code (code, active_key),
    KEY idx_branch_province (province_id),
    CONSTRAINT fk_branch_province FOREIGN KEY (province_id) REFERENCES province (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE store (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id   BIGINT       NOT NULL,
    code        VARCHAR(30)  NOT NULL,
    name        VARCHAR(200) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    updated_at  DATETIME(6)  NULL,
    updated_by  VARCHAR(100) NULL,
    deleted_at  DATETIME(6)  NULL,
    deleted_by  VARCHAR(100) NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    active_key  TINYINT GENERATED ALWAYS AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    UNIQUE KEY uk_store_code (code, active_key),
    KEY idx_store_branch (branch_id),
    CONSTRAINT fk_store_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE store_whitelist (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id    BIGINT       NOT NULL,
    note        VARCHAR(255) NULL,
    created_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    updated_at  DATETIME(6)  NULL,
    updated_by  VARCHAR(100) NULL,
    deleted_at  DATETIME(6)  NULL,
    deleted_by  VARCHAR(100) NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    active_key  TINYINT GENERATED ALWAYS AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    UNIQUE KEY uk_whitelist_store (store_id, active_key),
    CONSTRAINT fk_whitelist_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE app_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    created_by    VARCHAR(100) NOT NULL,
    updated_at    DATETIME(6)  NULL,
    updated_by    VARCHAR(100) NULL,
    deleted_at    DATETIME(6)  NULL,
    deleted_by    VARCHAR(100) NULL,
    version       BIGINT       NOT NULL DEFAULT 0,
    active_key    TINYINT GENERATED ALWAYS AS (IF(deleted_at IS NULL, 1, NULL)) STORED,
    UNIQUE KEY uk_app_user_username (username, active_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    action      VARCHAR(20)  NOT NULL,
    old_value   LONGTEXT     NULL,
    new_value   LONGTEXT     NULL,
    username    VARCHAR(100) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    KEY idx_audit_entity (entity_type, entity_id),
    KEY idx_audit_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TRIGGER trg_audit_log_no_update BEFORE UPDATE ON audit_log
    FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'audit_log is append-only';

CREATE TRIGGER trg_audit_log_no_delete BEFORE DELETE ON audit_log
    FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'audit_log is append-only';
