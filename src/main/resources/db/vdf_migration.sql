-- Migration to update vdf_family_config table structure
-- Run this script manually in PostgreSQL

-- Add new required columns
ALTER TABLE vdf_family_config 
ADD COLUMN IF NOT EXISTS family_head_name VARCHAR(200),
ADD COLUMN IF NOT EXISTS is_contribution_enabled BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS effective_from DATE,
ADD COLUMN IF NOT EXISTS monthly_amount NUMERIC(10,2) DEFAULT 20.00;

-- Update existing records to have default values
UPDATE vdf_family_config 
SET family_head_name = 'Unknown'
WHERE family_head_name IS NULL;

-- Now make family_head_name NOT NULL
ALTER TABLE vdf_family_config 
ALTER COLUMN family_head_name SET NOT NULL;

-- Drop unused columns if they exist
ALTER TABLE vdf_family_config 
DROP COLUMN IF EXISTS family_size,
DROP COLUMN IF EXISTS joined_date;

-- Add index for performance
CREATE INDEX IF NOT EXISTS idx_vdf_family_contribution_enabled 
ON vdf_family_config(is_contribution_enabled);

-- Add constraint for monthly amount
ALTER TABLE vdf_family_config 
ADD CONSTRAINT check_monthly_amount_positive 
CHECK (monthly_amount >= 0);

-- Update vdf_contributions table to link to family_config instead of family
-- First, add the new column
ALTER TABLE vdf_contributions
ADD COLUMN IF NOT EXISTS family_config_id UUID;

-- Copy data from family_id to family_config_id if not already done
UPDATE vdf_contributions
SET family_config_id = family_id
WHERE family_config_id IS NULL;

-- Now we can drop the old foreign key and column (if exists)
ALTER TABLE vdf_contributions
DROP CONSTRAINT IF EXISTS vdf_contributions_family_id_fkey;

-- Make family_config_id NOT NULL
ALTER TABLE vdf_contributions
ALTER COLUMN family_config_id SET NOT NULL;

-- Add new foreign key
ALTER TABLE vdf_contributions
ADD CONSTRAINT fk_vdf_contributions_family_config
FOREIGN KEY (family_config_id) REFERENCES vdf_family_config(id) ON DELETE CASCADE;

-- Add unique constraint for family + year + month
ALTER TABLE vdf_contributions
ADD CONSTRAINT unique_family_year_month 
UNIQUE (family_config_id, year, month);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_vdf_contributions_family_config 
ON vdf_contributions(family_config_id);

CREATE INDEX IF NOT EXISTS idx_vdf_contributions_year_month 
ON vdf_contributions(year, month);
