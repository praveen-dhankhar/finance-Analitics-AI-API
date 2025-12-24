-- Forecast configuration and results tables

CREATE TABLE IF NOT EXISTS forecast_configs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    algorithm VARCHAR(64) NOT NULL,
    window_size INTEGER,
    smoothing_factor DOUBLE PRECISION,
    season_length INTEGER,
    category VARCHAR(128),
    transaction_type VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_forecast_configs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_forecast_configs_user ON forecast_configs(user_id);
CREATE INDEX IF NOT EXISTS idx_forecast_configs_algo ON forecast_configs(algorithm);

CREATE TABLE IF NOT EXISTS forecast_results (
    id BIGSERIAL PRIMARY KEY,
    config_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    target_date DATE NOT NULL,
    forecast_value NUMERIC(19,2) NOT NULL,
    confidence_low NUMERIC(19,2),
    confidence_high NUMERIC(19,2),
    mape DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_forecast_results_config FOREIGN KEY (config_id) REFERENCES forecast_configs(id),
    CONSTRAINT fk_forecast_results_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_forecast_results_user_date ON forecast_results(user_id, target_date);
CREATE INDEX IF NOT EXISTS idx_forecast_results_config ON forecast_results(config_id);

