-- PostgreSQL-specific indexes, JSON columns, and FTS setup

-- Enable pg_trgm for LIKE performance (optional)
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Add GIN index on JSON metadata fields (example table: forecast_results has no JSON; using params_json in anomalies)
CREATE INDEX IF NOT EXISTS idx_forecast_anom_params_gin ON forecast_anomalies USING gin ((params_json));

-- Full Text Search example on categories name (if exists); comment safe for H2 as it's in PG path
-- CREATE INDEX IF NOT EXISTS idx_categories_name_fts ON categories USING gin (to_tsvector('simple', name));

-- Performance index samples
CREATE INDEX IF NOT EXISTS idx_forecast_results_user_date ON forecast_results (user_id, target_date);
CREATE INDEX IF NOT EXISTS idx_forecast_config_user_algo ON forecast_configs (user_id, algorithm);
