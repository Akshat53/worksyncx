-- Create shifts table
CREATE TABLE shifts (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    grace_period_minutes INTEGER DEFAULT 15,
    half_day_hours DECIMAL(4,2) DEFAULT 4.0,
    full_day_hours DECIMAL(4,2) DEFAULT 8.0,
    color VARCHAR(7) DEFAULT '#3B82F6',
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT shifts_tenant_code_unique UNIQUE (tenant_id, code),
    CONSTRAINT shifts_valid_times CHECK (end_time > start_time),
    CONSTRAINT shifts_valid_grace CHECK (grace_period_minutes >= 0 AND grace_period_minutes <= 120)
);

-- Create employee_shifts table for shift assignments
CREATE TABLE employee_shifts (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    shift_id BIGINT NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    days_of_week VARCHAR(20)[] NOT NULL DEFAULT ARRAY['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
    is_active BOOLEAN DEFAULT true,
    assigned_by BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_shift_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_shift_shift FOREIGN KEY (shift_id) REFERENCES shifts(id) ON DELETE RESTRICT,
    CONSTRAINT employee_shifts_valid_dates CHECK (effective_to IS NULL OR effective_to >= effective_from),
    CONSTRAINT employee_shifts_days_not_empty CHECK (array_length(days_of_week, 1) > 0)
);

-- Add shift-related columns to attendance_records
ALTER TABLE attendance_records
ADD COLUMN shift_id BIGINT,
ADD COLUMN expected_start_time TIME,
ADD COLUMN expected_end_time TIME,
ADD COLUMN late_by_minutes INTEGER DEFAULT 0,
ADD COLUMN early_leave_by_minutes INTEGER DEFAULT 0,
ADD CONSTRAINT fk_attendance_shift FOREIGN KEY (shift_id) REFERENCES shifts(id) ON DELETE SET NULL;

-- Create indexes for better performance
CREATE INDEX idx_shifts_tenant_active ON shifts(tenant_id, is_active);
CREATE INDEX idx_shifts_code ON shifts(code);

CREATE INDEX idx_employee_shifts_employee ON employee_shifts(employee_id);
CREATE INDEX idx_employee_shifts_shift ON employee_shifts(shift_id);
CREATE INDEX idx_employee_shifts_dates ON employee_shifts(effective_from, effective_to);
CREATE INDEX idx_employee_shifts_active ON employee_shifts(is_active);

CREATE INDEX idx_attendance_shift ON attendance_records(shift_id);

-- Insert default shifts for all existing tenants
INSERT INTO shifts (tenant_id, name, code, start_time, end_time, grace_period_minutes, half_day_hours, full_day_hours, color, description)
SELECT DISTINCT tenant_id,
    'Regular Shift',
    'REGULAR',
    '09:00:00'::TIME,
    '17:00:00'::TIME,
    15,
    4.0,
    8.0,
    '#3B82F6',
    'Standard 9 AM to 5 PM shift with 15 minutes grace period'
FROM employees
WHERE NOT EXISTS (
    SELECT 1 FROM shifts WHERE shifts.tenant_id = employees.tenant_id AND shifts.code = 'REGULAR'
);

-- Assign default shift to all existing active employees
INSERT INTO employee_shifts (employee_id, shift_id, effective_from, days_of_week, is_active)
SELECT
    e.id,
    s.id,
    CURRENT_DATE,
    ARRAY['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
    true
FROM employees e
INNER JOIN shifts s ON s.tenant_id = e.tenant_id AND s.code = 'REGULAR'
WHERE e.employment_status = 'ACTIVE'
AND NOT EXISTS (
    SELECT 1 FROM employee_shifts es WHERE es.employee_id = e.id AND es.is_active = true
);

-- Update existing attendance records with shift information
UPDATE attendance_records ar
SET
    shift_id = es.shift_id,
    expected_start_time = s.start_time,
    expected_end_time = s.end_time,
    late_by_minutes = CASE
        WHEN ar.check_in_time IS NOT NULL AND ar.check_in_time > (s.start_time + (s.grace_period_minutes || ' minutes')::INTERVAL)
        THEN EXTRACT(EPOCH FROM (ar.check_in_time::TIME - s.start_time)) / 60
        ELSE 0
    END::INTEGER,
    early_leave_by_minutes = CASE
        WHEN ar.check_out_time IS NOT NULL AND ar.check_out_time < s.end_time
        THEN EXTRACT(EPOCH FROM (s.end_time - ar.check_out_time::TIME)) / 60
        ELSE 0
    END::INTEGER
FROM employee_shifts es
INNER JOIN shifts s ON s.id = es.shift_id
WHERE ar.employee_id = es.employee_id
AND ar.attendance_date BETWEEN es.effective_from AND COALESCE(es.effective_to, CURRENT_DATE)
AND ar.shift_id IS NULL;

-- Create function to get active shift for an employee on a given date
CREATE OR REPLACE FUNCTION get_employee_shift(
    p_employee_id BIGINT,
    p_date DATE
) RETURNS TABLE (
    shift_id BIGINT,
    shift_name VARCHAR,
    shift_code VARCHAR,
    start_time TIME,
    end_time TIME,
    grace_period_minutes INTEGER,
    color VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        s.id,
        s.name,
        s.code,
        s.start_time,
        s.end_time,
        s.grace_period_minutes,
        s.color
    FROM employee_shifts es
    INNER JOIN shifts s ON s.id = es.shift_id
    WHERE es.employee_id = p_employee_id
    AND es.is_active = true
    AND p_date >= es.effective_from
    AND (es.effective_to IS NULL OR p_date <= es.effective_to)
    AND TO_CHAR(p_date, 'DAY') = ANY(es.days_of_week)
    ORDER BY es.effective_from DESC
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

-- Create function to auto-assign default shift to new employees
CREATE OR REPLACE FUNCTION assign_default_shift_to_employee()
RETURNS TRIGGER AS $$
DECLARE
    v_default_shift_id BIGINT;
BEGIN
    -- Get default shift for tenant
    SELECT id INTO v_default_shift_id
    FROM shifts
    WHERE tenant_id = NEW.tenant_id
    AND code = 'REGULAR'
    AND is_active = true
    LIMIT 1;

    -- Assign shift if found
    IF v_default_shift_id IS NOT NULL THEN
        INSERT INTO employee_shifts (
            employee_id,
            shift_id,
            effective_from,
            days_of_week,
            is_active
        ) VALUES (
            NEW.id,
            v_default_shift_id,
            COALESCE(NEW.joining_date, CURRENT_DATE),
            ARRAY['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
            true
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to auto-assign shift when new employee is created
CREATE TRIGGER trigger_assign_default_shift
AFTER INSERT ON employees
FOR EACH ROW
WHEN (NEW.employment_status = 'ACTIVE')
EXECUTE FUNCTION assign_default_shift_to_employee();

-- Add comment to tables
COMMENT ON TABLE shifts IS 'Stores shift definitions with timing and rules';
COMMENT ON TABLE employee_shifts IS 'Maps employees to shifts with effective date ranges and working days';
COMMENT ON COLUMN shifts.grace_period_minutes IS 'Grace period in minutes after shift start time before marking as late';
COMMENT ON COLUMN shifts.half_day_hours IS 'Minimum hours required for half day';
COMMENT ON COLUMN shifts.full_day_hours IS 'Minimum hours required for full day';
COMMENT ON COLUMN employee_shifts.days_of_week IS 'Array of working days: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY';
COMMENT ON COLUMN attendance_records.late_by_minutes IS 'Number of minutes late from expected start time';
COMMENT ON COLUMN attendance_records.early_leave_by_minutes IS 'Number of minutes left early before expected end time';
