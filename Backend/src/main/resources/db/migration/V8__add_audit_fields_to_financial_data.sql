-- Add audit fields to financial_data table
-- This migration adds the missing created_at and updated_at columns

-- Add created_at column
ALTER TABLE financial_data 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE;

-- Add updated_at column  
ALTER TABLE financial_data 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- Update existing records to have current timestamp
UPDATE financial_data 
SET created_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP 
WHERE created_at IS NULL OR updated_at IS NULL;
