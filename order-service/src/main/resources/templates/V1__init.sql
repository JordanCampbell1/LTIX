CREATE TABLE orders (
    id uuid PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    side VARCHAR(10) NOT NULL,
    quantity BIGINT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE executions (
    id uuid PRIMARY KEY,
    order_id uuid NOT NULL,
    executed_price DOUBLE PRECISION NOT NULL,
    latency_ms BIGINT NOT NULL,
    executed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_executions_order_id
    ON executions(order_id);
