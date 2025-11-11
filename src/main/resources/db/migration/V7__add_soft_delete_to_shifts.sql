-- Add soft delete columns to shifts table
ALTER TABLE shifts
ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by BIGINT;

-- Add foreign key constraint for deleted_by
ALTER TABLE shifts
ADD CONSTRAINT fk_shifts_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id);

-- Add index for soft delete queries
CREATE INDEX idx_shifts_is_deleted ON shifts(is_deleted);

-- Add comment
COMMENT ON COLUMN shifts.is_deleted IS 'Soft delete flag - true if shift is deleted';
COMMENT ON COLUMN shifts.deleted_at IS 'Timestamp when shift was soft deleted';
COMMENT ON COLUMN shifts.deleted_by IS 'User ID who soft deleted the shift';
