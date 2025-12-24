-- Add indexes for audit fields in financial_data table
-- This migration creates indexes on the created_at and updated_at columns

-- Created/Updated timestamp indexes for audit queries
CREATE INDEX IF NOT EXISTS idx_financial_data_created_at ON financial_data(created_at);
CREATE INDEX IF NOT EXISTS idx_financial_data_updated_at ON financial_data(updated_at);

-- Composite index for recent data queries (H2 compatible)
CREATE INDEX IF NOT EXISTS idx_financial_data_user_created_desc ON financial_data(user_id, created_at);
