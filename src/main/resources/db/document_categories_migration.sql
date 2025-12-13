-- Migration to create document_categories and member_documents tables
-- Run this script manually in PostgreSQL

-- Create document_categories table
CREATE TABLE IF NOT EXISTS document_categories (
    id UUID PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create member_documents table
CREATE TABLE IF NOT EXISTS member_documents (
    id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    document_category_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    object_storage_path VARCHAR(500) NOT NULL,
    uploaded_by UUID NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    notes VARCHAR(1000),
    CONSTRAINT fk_member_document_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_document_category FOREIGN KEY (document_category_id) REFERENCES document_categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_member_document_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES members(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_member_documents_member_id ON member_documents(member_id);
CREATE INDEX IF NOT EXISTS idx_member_documents_category_id ON member_documents(document_category_id);
CREATE INDEX IF NOT EXISTS idx_document_categories_active ON document_categories(is_active);

-- Insert default document categories
INSERT INTO document_categories (id, category_name, description, is_active, created_at, updated_at)
VALUES 
    (gen_random_uuid(), 'Aadhaar', 'Aadhaar Card', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'PAN', 'PAN Card', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Ration Card', 'Ration Card', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Birth Certificate', 'Birth Certificate', TRUE, NOW(), NOW())
ON CONFLICT (category_name) DO NOTHING;

