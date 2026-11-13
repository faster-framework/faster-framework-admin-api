-- ----------------------------------------------------------------------------
-- H2（MODE=MySQL）测试库结构，与 db/faster-admin.sql 保持一致
-- ----------------------------------------------------------------------------

DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_dict;

CREATE TABLE sys_user
(
    id          BIGINT       NOT NULL,
    account     VARCHAR(64)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    create_by   BIGINT       NULL,
    update_by   BIGINT       NULL,
    create_date DATETIME     NOT NULL,
    update_date DATETIME     NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(1024) NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_account UNIQUE (account)
);

CREATE TABLE sys_role
(
    id          BIGINT      NOT NULL,
    name        VARCHAR(64) NOT NULL,
    create_by   BIGINT      NULL,
    update_by   BIGINT      NULL,
    create_date DATETIME    NOT NULL,
    update_date DATETIME    NOT NULL,
    sort        INT         NOT NULL DEFAULT 0,
    remark      VARCHAR(1024) NULL,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE sys_permission
(
    id          BIGINT        NOT NULL,
    name        VARCHAR(64)   NOT NULL,
    code        VARCHAR(64)   NOT NULL,
    parent_id   BIGINT        NOT NULL,
    parent_ids  VARCHAR(2048) NOT NULL,
    create_by   BIGINT        NULL,
    update_by   BIGINT        NULL,
    create_date DATETIME      NOT NULL,
    update_date DATETIME      NOT NULL,
    sort        INT           NOT NULL DEFAULT 0,
    remark      VARCHAR(1024) NULL,
    deleted     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE sys_user_role
(
    id          BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    role_id     BIGINT   NOT NULL,
    create_by   BIGINT   NULL,
    update_by   BIGINT   NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    sort        INT      NOT NULL DEFAULT 0,
    remark      VARCHAR(1024) NULL,
    deleted     TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE sys_role_permission
(
    id            BIGINT   NOT NULL,
    role_id       BIGINT   NOT NULL,
    permission_id BIGINT   NOT NULL,
    create_by     BIGINT   NULL,
    update_by     BIGINT   NULL,
    create_date   DATETIME NOT NULL,
    update_date   DATETIME NOT NULL,
    sort          INT      NOT NULL DEFAULT 0,
    remark        VARCHAR(1024) NULL,
    deleted       TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id)
);

CREATE TABLE sys_dict
(
    id          BIGINT       NOT NULL,
    name        VARCHAR(128) NULL,
    type        VARCHAR(128) NULL,
    dict_value  TEXT         NULL,
    show_status TINYINT      NOT NULL DEFAULT 1,
    create_by   BIGINT       NULL,
    update_by   BIGINT       NULL,
    create_date DATETIME     NOT NULL,
    update_date DATETIME     NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(1024) NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX idx_sys_permission_parent ON sys_permission (parent_id);
CREATE INDEX idx_sys_permission_code ON sys_permission (code);
CREATE INDEX idx_sys_role_name ON sys_role (name);
CREATE INDEX idx_sys_dict_type ON sys_dict (type);
CREATE INDEX idx_sys_dict_name ON sys_dict (name);
