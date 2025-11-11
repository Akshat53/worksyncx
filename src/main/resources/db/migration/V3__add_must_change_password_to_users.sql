-- Add must_change_password column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Add comment
COMMENT ON COLUMN users.must_change_password IS 'Flag to indicate if user must change password on next login';
