-- Add primary owner tracking to tenants table
-- This prevents orphaned tenants when admins delete their accounts

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS primary_owner_user_id BIGINT;

-- Add foreign key constraint to users table
ALTER TABLE tenants ADD CONSTRAINT fk_tenants_primary_owner
    FOREIGN KEY (primary_owner_user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- Add comment to explain the column's purpose
COMMENT ON COLUMN tenants.primary_owner_user_id IS 'User ID of the account that created and owns this tenant (purchased subscription). Cannot be deleted.';

-- Update existing tenants with the first TENANT_ADMIN as primary owner
UPDATE tenants t
SET primary_owner_user_id = (
    SELECT u.id
    FROM users u
    JOIN user_roles ur ON u.id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    WHERE u.tenant_id = t.id
      AND r.name = 'TENANT_ADMIN'
      AND u.is_active = TRUE
    ORDER BY u.created_at ASC
    LIMIT 1
)
WHERE primary_owner_user_id IS NULL;
