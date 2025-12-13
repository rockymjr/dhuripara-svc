-- Migration to allow multiple members per family
-- Run this script manually in PostgreSQL

-- Remove unique constraint on member_id in vdf_family_config
-- This allows multiple members to link to the same family via family_id
ALTER TABLE vdf_family_config 
DROP CONSTRAINT IF EXISTS vdf_family_config_member_id_key;

-- Note: The member_id in vdf_family_config represents the primary member/head
-- Other members can link to the same family via family_id in members table

