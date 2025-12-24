-- This migration only runs for dev profile, not test profile
-- Test profile uses @DataJpaTest which doesn't run Flyway migrations

-- Seed users
INSERT INTO users (username, email, password_hash)
VALUES ('demo', 'demo@example.com', 'hashed-password-123');

-- Seed categories for demo user
INSERT INTO categories (name, type, color, user_id, is_system, is_active, sort_order, created_at, updated_at)
SELECT 'FOOD', 'EXPENSE', '#43a047', id, false, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE username = 'demo';

INSERT INTO categories (name, type, color, user_id, is_system, is_active, sort_order, created_at, updated_at)
SELECT 'SALARY', 'INCOME', '#1e88e5', id, false, true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE username = 'demo';

-- Seed financial data
INSERT INTO financial_data (user_id, date, amount, category, description, type)
SELECT id, CURRENT_DATE, 100.00, 'FOOD', 'Weekly grocery shopping', 'EXPENSE' FROM users WHERE username = 'demo';

INSERT INTO financial_data (user_id, date, amount, category, description, type)
SELECT id, CURRENT_DATE, 2000.00, 'SALARY', 'Monthly salary', 'INCOME' FROM users WHERE username = 'demo';

-- Seed forecast
INSERT INTO forecasts (user_id, forecast_date, predicted_amount, confidence_score, model_name, model_version, prediction_context, forecast_type, status)
SELECT id, CURRENT_DATE + 30, 1800.00, 0.85, 'Demo Model v1.0', '1.0.0', 'Demo prediction based on historical data', 'INCOME_EXPENSE', 'ACTIVE' FROM users WHERE username = 'demo';

