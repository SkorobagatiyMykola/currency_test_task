drop table if exists currencies;

CREATE TABLE currencies(
    date_id date,
    currency_id int,
    code VARCHAR(3) NOT NULL,
    name VARCHAR(100) NOT NULL,
    rate decimal(20,8),
    created_at timestamptz default CURRENT_TIMESTAMP,
    PRIMARY KEY(date_id, currency_id)
);