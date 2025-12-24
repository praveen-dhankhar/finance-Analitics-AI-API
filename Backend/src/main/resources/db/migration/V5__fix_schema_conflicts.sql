-- Fix schema conflicts between existing data and new JPA entities

-- Drop existing data that conflicts with new structure
DELETE FROM financial_data WHERE category NOT IN (
    'SALARY', 'BONUS', 'FREELANCE', 'INVESTMENT_RETURN', 'RENTAL_INCOME', 'OTHER_INCOME',
    'HOUSING', 'FOOD', 'TRANSPORTATION', 'HEALTHCARE', 'ENTERTAINMENT', 'EDUCATION', 
    'SHOPPING', 'UTILITIES', 'INSURANCE', 'TAXES', 'DEBT_PAYMENT', 'SAVINGS', 
    'INVESTMENT', 'OTHER_EXPENSE'
);

-- Update existing data to match new enum values
UPDATE financial_data SET category = 'FOOD' WHERE category = 'Groceries';
UPDATE financial_data SET category = 'ENTERTAINMENT' WHERE category = 'Movies';
UPDATE financial_data SET category = 'TRANSPORTATION' WHERE category = 'Gas';

-- Fix confidence score values that are too large
UPDATE forecasts SET confidence_score = 0.85 WHERE confidence_score > 1.0;
