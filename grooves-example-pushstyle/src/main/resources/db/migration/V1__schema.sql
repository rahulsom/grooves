CREATE TABLE balance (
    b_id      VARCHAR(36),
    b_version NUMERIC(12, 0),
    b_time    TIMESTAMP WITH TIME ZONE,
    b_account VARCHAR(36),
    balance   NUMERIC(12, 0)
);