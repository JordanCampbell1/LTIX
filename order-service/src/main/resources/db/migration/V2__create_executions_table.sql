-- Create executions table
CREATE TABLE executions (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    executed_price NUMERIC(19, 6) NOT NULL,
    latency_ms BIGINT NOT NULL,
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for fast lookups by order_id
CREATE INDEX idx_executions_order_id ON executions(order_id);

-- Index for queries ordered by executed_at
CREATE INDEX idx_executions_executed_at ON executions(executed_at);

-- Composite index for queries by order_id and executed_at
CREATE INDEX idx_executions_order_id_executed_at ON executions(order_id, executed_at);
