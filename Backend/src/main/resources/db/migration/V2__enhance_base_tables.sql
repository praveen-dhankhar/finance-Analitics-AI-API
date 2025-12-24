-- Add constraints and indexes to users
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_username ON users (username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users (email);

-- FKs were created in V1; only add indexes here to avoid duplicate constraint errors
CREATE INDEX IF NOT EXISTS idx_financial_data_user_date ON financial_data (user_id, date);
CREATE INDEX IF NOT EXISTS idx_financial_data_category ON financial_data (category);

-- FKs were created in V1; only add indexes here
CREATE INDEX IF NOT EXISTS idx_forecasts_user_date ON forecasts (user_id, forecast_date);

-- Checks (portable for H2 and Postgres)
ALTER TABLE financial_data
	ADD CONSTRAINT chk_financial_data_amount_positive CHECK (amount >= 0);

ALTER TABLE forecasts
	ADD CONSTRAINT chk_forecasts_confidence_range CHECK (confidence_score >= 0 AND confidence_score <= 100);

-- Ensure updated_at auto-updates, using triggers for Postgres; H2 will ignore the trigger syntax
-- For H2 we simulate by default value; for Postgres, a trigger can be created later when switching
-- Keep column with default for both
ALTER TABLE users ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

