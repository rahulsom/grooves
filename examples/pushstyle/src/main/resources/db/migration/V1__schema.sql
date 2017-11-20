CREATE TABLE balance (
    b_id      VARCHAR(36),
    b_version BIGINT,
    b_time    DATETIME,
    b_account VARCHAR(36),
    balance   BIGINT
);