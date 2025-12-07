-- Migration: Add VDF Deposit Categories Table
-- This creates a table to store deposit source categories

-- Create vdf_deposit_categories table
CREATE TABLE IF NOT EXISTS vdf_deposit_categories (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    PRIMARY KEY (id)
);

ALTER TABLE vdf_deposit_categories OWNER TO postgres;

-- Add foreign key to vdf_deposits table to link deposits to categories
ALTER TABLE vdf_deposits 
ADD COLUMN IF NOT EXISTS deposit_category_id UUID;

-- Add foreign key constraint
ALTER TABLE vdf_deposits
ADD CONSTRAINT IF NOT EXISTS fk_vdf_deposits_category
FOREIGN KEY (deposit_category_id) REFERENCES vdf_deposit_categories(id) ON DELETE SET NULL;

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_vdf_deposit_categories_active 
ON vdf_deposit_categories(is_active);

-- Insert default deposit categories
INSERT INTO vdf_deposit_categories (id, category_name, description, is_active) 
VALUES 
    ('123e4567-e89b-12d3-a456-426614174000'::uuid, 'Villager Contribution', 'Monthly contribution from villagers', true),
    ('223e4567-e89b-12d3-a456-426614174001'::uuid, 'Donation', 'Direct donations from individuals or organizations', true),
    ('323e4567-e89b-12d3-a456-426614174002'::uuid, 'Government Grant', 'Grants received from government bodies', true),
    ('423e4567-e89b-12d3-a456-426614174003'::uuid, 'NGO Support', 'Support from NGOs', true),
    ('523e4567-e89b-12d3-a456-426614174004'::uuid, 'Other Income', 'Other sources of income', true)
ON CONFLICT DO NOTHING;
