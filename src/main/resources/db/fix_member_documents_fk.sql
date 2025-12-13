-- Migration: Fix member_documents.uploaded_by foreign key to reference members table
-- This script alters the foreign key constraint so that uploaded_by references members(id)
ALTER TABLE IF EXISTS member_documents
  DROP CONSTRAINT IF EXISTS fk_member_document_uploaded_by;

ALTER TABLE IF EXISTS member_documents
  ADD CONSTRAINT fk_member_document_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES members(id) ON DELETE RESTRICT;

-- End of migration
