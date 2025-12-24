-- Add missing columns to forecasts table
ALTER TABLE forecasts ADD COLUMN model_name VARCHAR(100);
ALTER TABLE forecasts ADD COLUMN model_version VARCHAR(50);
ALTER TABLE forecasts ADD COLUMN prediction_context TEXT;
ALTER TABLE forecasts ADD COLUMN forecast_type VARCHAR(50);
ALTER TABLE forecasts ADD COLUMN status VARCHAR(50);

-- Update existing records with default values
UPDATE forecasts SET 
    model_name = 'Default Model',
    model_version = '1.0.0',
    prediction_context = 'Default prediction context',
    forecast_type = 'INCOME_EXPENSE',
    status = 'ACTIVE',
    confidence_score = 85.0
WHERE model_name IS NULL;

-- Make model_name NOT NULL after setting default values
ALTER TABLE forecasts ALTER COLUMN model_name SET NOT NULL;
ALTER TABLE forecasts ALTER COLUMN forecast_type SET NOT NULL;
ALTER TABLE forecasts ALTER COLUMN status SET NOT NULL;

-- Add indexes for the new columns
CREATE INDEX idx_forecasts_model ON forecasts (model_name);
CREATE INDEX idx_forecasts_model_version ON forecasts (model_version);
CREATE INDEX idx_forecasts_type ON forecasts (forecast_type);
CREATE INDEX idx_forecasts_status ON forecasts (status);
