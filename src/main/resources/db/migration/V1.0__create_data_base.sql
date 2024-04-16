--drop table if exists currencies;

CREATE TABLE IF NOT EXISTS currencies(
    date_id date,
    currency_id int,
    code VARCHAR(3) NOT NULL,
    name VARCHAR(100) NOT NULL,
    rate decimal(20,8),
    created_at timestamptz,
    PRIMARY KEY(date_id, currency_id)
);