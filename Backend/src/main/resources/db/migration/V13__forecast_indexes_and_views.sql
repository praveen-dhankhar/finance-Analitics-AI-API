-- Forecast indexes and materialized view scaffolds (DB-agnostic where possible)

CREATE INDEX IF NOT EXISTS idx_forecast_results_user_date ON forecast_results(user_id, target_date);
CREATE INDEX IF NOT EXISTS idx_forecast_results_mape ON forecast_results(mape);

-- Materialized view concept (commented; enable per database manually)
-- PostgreSQL example:
-- CREATE MATERIALIZED VIEW IF NOT EXISTS mv_user_daily_totals AS
-- SELECT user_id, date, SUM(amount) AS total
-- FROM financial_data
-- GROUP BY user_id, date;
-- CREATE INDEX IF NOT EXISTS idx_mv_user_daily_totals ON mv_user_daily_totals(user_id, date);

-- H2 note: H2 does not support materialized views; consider periodic table refresh via application logic.


