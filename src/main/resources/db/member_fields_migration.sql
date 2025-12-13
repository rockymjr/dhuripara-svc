-- Migration to add new fields to members table
-- Run this script manually in PostgreSQL

-- Add new columns for member personal information
ALTER TABLE members 
ADD COLUMN IF NOT EXISTS date_of_birth DATE,
ADD COLUMN IF NOT EXISTS aadhar_no VARCHAR(12),
ADD COLUMN IF NOT EXISTS voter_no VARCHAR(20),
ADD COLUMN IF NOT EXISTS pan_no VARCHAR(10),
ADD COLUMN IF NOT EXISTS family_id UUID;

-- Add foreign key constraint for family relationship
ALTER TABLE members
ADD CONSTRAINT fk_member_family
FOREIGN KEY (family_id) REFERENCES vdf_family_config(id) ON DELETE SET NULL;

-- Create index for family_id for better query performance
CREATE INDEX IF NOT EXISTS idx_members_family_id ON members(family_id);

-- Add indexes for searchable fields
CREATE INDEX IF NOT EXISTS idx_members_aadhar ON members(aadhar_no) WHERE aadhar_no IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_members_voter ON members(voter_no) WHERE voter_no IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_members_pan ON members(pan_no) WHERE pan_no IS NOT NULL;

