-- Financial Data Indexes for Performance Optimization
-- Compatible with both H2 and PostgreSQL

-- User ID index for user-specific queries
CREATE INDEX IF NOT EXISTS idx_financial_data_user_id ON financial_data(user_id);

-- Type index for filtering by transaction type
CREATE INDEX IF NOT EXISTS idx_financial_data_type ON financial_data(type);

-- Category index for filtering by category
CREATE INDEX IF NOT EXISTS idx_financial_data_category ON financial_data(category);

-- Date index for date range queries
CREATE INDEX IF NOT EXISTS idx_financial_data_date ON financial_data(date);

-- Amount index for amount range queries
CREATE INDEX IF NOT EXISTS idx_financial_data_amount ON financial_data(amount);

-- Composite indexes for common query patterns

-- User + Type + Date (most common pattern)
CREATE INDEX IF NOT EXISTS idx_financial_data_user_type_date ON financial_data(user_id, type, date);

-- User + Category + Date
CREATE INDEX IF NOT EXISTS idx_financial_data_user_category_date ON financial_data(user_id, category, date);

-- User + Date range queries (H2 compatible)
CREATE INDEX IF NOT EXISTS idx_financial_data_user_date_range ON financial_data(user_id, date);

-- User + Amount range queries
CREATE INDEX IF NOT EXISTS idx_financial_data_user_amount_range ON financial_data(user_id, amount);

-- User + Type + Category (for category analysis)
CREATE INDEX IF NOT EXISTS idx_financial_data_user_type_category ON financial_data(user_id, type, category);

-- Description search index (for text search)
CREATE INDEX IF NOT EXISTS idx_financial_data_description ON financial_data(description);

-- Created/Updated timestamp indexes for audit queries
-- These will be created after the audit fields are added in V8
-- CREATE INDEX IF NOT EXISTS idx_financial_data_created_at ON financial_data(created_at);
-- CREATE INDEX IF NOT EXISTS idx_financial_data_updated_at ON financial_data(updated_at);

-- Composite index for recent data queries (H2 compatible)
-- CREATE INDEX IF NOT EXISTS idx_financial_data_user_created_desc ON financial_data(user_id, created_at);

-- Partial indexes for active data (if soft delete is implemented)
-- CREATE INDEX IF NOT EXISTS idx_financial_data_active ON financial_data(user_id, date) WHERE deleted_at IS NULL;

-- PostgreSQL-specific optimizations (commented for H2 compatibility)
-- These would be uncommented when migrating to PostgreSQL

-- GIN index for JSON fields (if JSON columns are added)
-- CREATE INDEX IF NOT EXISTS idx_financial_data_metadata_gin ON financial_data USING GIN(metadata);

-- BRIN index for large date ranges (PostgreSQL 9.5+)
-- CREATE INDEX IF NOT EXISTS idx_financial_data_date_brin ON financial_data USING BRIN(date);

-- Partial index for high-value transactions
-- CREATE INDEX IF NOT EXISTS idx_financial_data_high_value ON financial_data(user_id, date, amount) WHERE amount > 1000;

-- Covering index for common aggregation queries
-- CREATE INDEX IF NOT EXISTS idx_financial_data_covering ON financial_data(user_id, type, category, date, amount) INCLUDE (description);

-- Expression index for case-insensitive search
-- CREATE INDEX IF NOT EXISTS idx_financial_data_description_lower ON financial_data(LOWER(description));

-- Statistics update for PostgreSQL query planner
-- ANALYZE financial_data;
