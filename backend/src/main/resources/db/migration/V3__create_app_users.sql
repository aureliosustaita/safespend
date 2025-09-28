-- SafeSpend: JWT users table
CREATE TABLE IF NOT EXISTS app_users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(120) NOT NULL UNIQUE,
  password VARCHAR(120) NOT NULL,
  roles    VARCHAR(200) NOT NULL
);
