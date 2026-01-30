-- ========================================================
-- MIGRATION: Add payment_method and payment_intent_id columns
-- ========================================================

USE ProjectDB;

-- Check if columns exist before adding them
-- If the table structure is old, add the missing columns

ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50) DEFAULT 'card' COMMENT 'card, cash';

ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS payment_intent_id VARCHAR(255) DEFAULT NULL COMMENT 'Stripe payment intent ID or pseudo ID for cash';

-- Verify the columns were added
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'orders' AND TABLE_SCHEMA = 'ProjectDB'
ORDER BY ORDINAL_POSITION;

-- ========================================================
-- RESULT: Orders table now has payment tracking columns
-- ========================================================
