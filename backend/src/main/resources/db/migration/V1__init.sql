-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
  id          BIGSERIAL PRIMARY KEY,
  user_id     VARCHAR(64) NOT NULL,
  merchant    VARCHAR(128) NOT NULL,
  category    VARCHAR(64) NOT NULL,
  amount      NUMERIC(12,2) NOT NULL,
  timestamp   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tx_user_cat_time
  ON transactions(user_id, category, timestamp DESC);

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
  id         BIGSERIAL PRIMARY KEY,
  user_id    VARCHAR(64) NOT NULL,
  category   VARCHAR(64) NOT NULL,
  amount     NUMERIC(12,2) NOT NULL,
  median     NUMERIC(12,2) NOT NULL,
  mad        NUMERIC(12,2) NOT NULL,
  zscore     DOUBLE PRECISION NOT NULL,
  reason     TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_alerts_user_cat_time
  ON alerts(user_id, category, created_at DESC);
