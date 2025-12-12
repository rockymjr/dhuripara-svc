-- Migration to create vdf_notifications table
-- Run this script manually in PostgreSQL

-- Create vdf_notifications table
CREATE TABLE IF NOT EXISTS vdf_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID,
    title VARCHAR(200) NOT NULL,
    title_bn VARCHAR(200),
    message TEXT NOT NULL,
    message_bn TEXT,
    type VARCHAR(50) NOT NULL,
    related_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vdf_notifications_member
        FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_vdf_notifications_member_id 
ON vdf_notifications(member_id);

CREATE INDEX IF NOT EXISTS idx_vdf_notifications_is_read 
ON vdf_notifications(is_read);

CREATE INDEX IF NOT EXISTS idx_vdf_notifications_created_at 
ON vdf_notifications(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_vdf_notifications_type 
ON vdf_notifications(type);

-- Add comment
COMMENT ON TABLE vdf_notifications IS 'VDF notifications for members (null member_id means notification for all members)';

