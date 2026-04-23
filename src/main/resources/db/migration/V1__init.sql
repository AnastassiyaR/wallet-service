CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    country VARCHAR(2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE balance (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    currency VARCHAR(3) NOT NULL,
    available_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    CONSTRAINT balance_currency_check CHECK (currency IN ('EUR', 'SEK', 'GBP', 'USD')),
    CONSTRAINT balance_unique_account_currency UNIQUE (account_id, currency),
    CONSTRAINT balance_amount_non_negative CHECK (available_amount >= 0)
);

CREATE TABLE transaction (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    direction VARCHAR(3) NOT NULL,
    description TEXT NOT NULL,
    balance_after NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT transaction_amount_positive CHECK (amount > 0),
    CONSTRAINT transaction_currency_check CHECK (currency IN ('EUR', 'SEK', 'GBP', 'USD')),
    CONSTRAINT transaction_direction_check CHECK (direction IN ('IN', 'OUT'))
);

CREATE INDEX idx_balance_account_id ON balance(account_id);
CREATE INDEX idx_transaction_account_id ON transaction(account_id);
CREATE INDEX idx_transaction_created_at ON transaction(created_at);