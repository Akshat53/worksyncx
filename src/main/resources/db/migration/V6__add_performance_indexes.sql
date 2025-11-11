-- =====================================================
-- Database Performance Indexes Migration
-- Version: 2
-- Description: Adds indexes for commonly queried columns
--              to improve query performance by 10-100x
-- =====================================================

-- =====================================================
-- USERS TABLE INDEXES
-- =====================================================

-- Index for login queries (email lookup)
CREATE INDEX IF NOT EXISTS idx_users_email
ON users(email);

-- Index for tenant-based user queries
CREATE INDEX IF NOT EXISTS idx_users_tenant_id
ON users(tenant_id);

-- Composite index for tenant + email queries
CREATE INDEX IF NOT EXISTS idx_users_tenant_email
ON users(tenant_id, email);

-- =====================================================
-- EMPLOYEES TABLE INDEXES
-- =====================================================

-- Index for tenant-based employee queries (most common)
CREATE INDEX IF NOT EXISTS idx_employees_tenant_id
ON employees(tenant_id);

-- Index for department-based queries
CREATE INDEX IF NOT EXISTS idx_employees_department_id
ON employees(department_id);

-- Index for designation-based queries
CREATE INDEX IF NOT EXISTS idx_employees_designation_id
ON employees(designation_id);

-- Index for employment status filtering
CREATE INDEX IF NOT EXISTS idx_employees_status
ON employees(employment_status);

-- Index for employee code lookup
CREATE INDEX IF NOT EXISTS idx_employees_code
ON employees(employee_code);

-- Composite index for tenant + status queries (very common)
CREATE INDEX IF NOT EXISTS idx_employees_tenant_status
ON employees(tenant_id, employment_status);

-- Composite index for tenant + department queries
CREATE INDEX IF NOT EXISTS idx_employees_tenant_department
ON employees(tenant_id, department_id);

-- Index for user_id lookup (to find employee by user)
CREATE INDEX IF NOT EXISTS idx_employees_user_id
ON employees(user_id);

-- =====================================================
-- ATTENDANCE_RECORDS TABLE INDEXES
-- =====================================================

-- Index for employee-based attendance queries
CREATE INDEX IF NOT EXISTS idx_attendance_employee_id
ON attendance_records(employee_id);

-- Index for date-based queries
CREATE INDEX IF NOT EXISTS idx_attendance_date
ON attendance_records(date);

-- Index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_attendance_tenant_id
ON attendance_records(tenant_id);

-- Index for status filtering
CREATE INDEX IF NOT EXISTS idx_attendance_status
ON attendance_records(status);

-- Composite index for employee + date (most common query pattern)
CREATE INDEX IF NOT EXISTS idx_attendance_employee_date
ON attendance_records(employee_id, date DESC);

-- Composite index for tenant + date (dashboard queries)
CREATE INDEX IF NOT EXISTS idx_attendance_tenant_date
ON attendance_records(tenant_id, date DESC);

-- Composite index for tenant + date + status (filtered dashboard)
CREATE INDEX IF NOT EXISTS idx_attendance_tenant_date_status
ON attendance_records(tenant_id, date DESC, status);

-- =====================================================
-- DEPARTMENTS TABLE INDEXES
-- =====================================================

-- Index for tenant-based department queries
CREATE INDEX IF NOT EXISTS idx_departments_tenant_id
ON departments(tenant_id);

-- Index for department name lookup
CREATE INDEX IF NOT EXISTS idx_departments_name
ON departments(name);

-- Composite index for tenant + name (unique constraint support)
CREATE INDEX IF NOT EXISTS idx_departments_tenant_name
ON departments(tenant_id, name);

-- =====================================================
-- DESIGNATIONS TABLE INDEXES
-- =====================================================

-- Index for tenant-based designation queries
CREATE INDEX IF NOT EXISTS idx_designations_tenant_id
ON designations(tenant_id);

-- Index for designation title lookup
CREATE INDEX IF NOT EXISTS idx_designations_title
ON designations(title);

-- Composite index for tenant + title
CREATE INDEX IF NOT EXISTS idx_designations_tenant_title
ON designations(tenant_id, title);

-- =====================================================
-- SHIFTS TABLE INDEXES
-- =====================================================

-- Index for tenant-based shift queries
CREATE INDEX IF NOT EXISTS idx_shifts_tenant_id
ON shifts(tenant_id);

-- Index for shift code lookup
CREATE INDEX IF NOT EXISTS idx_shifts_code
ON shifts(code);

-- Index for active status filtering
CREATE INDEX IF NOT EXISTS idx_shifts_is_active
ON shifts(is_active);

-- Composite index for tenant + active shifts
CREATE INDEX IF NOT EXISTS idx_shifts_tenant_active
ON shifts(tenant_id, is_active);

-- =====================================================
-- SHIFT_ASSIGNMENTS TABLE INDEXES
-- =====================================================

-- Index for employee-based shift assignment queries
CREATE INDEX IF NOT EXISTS idx_shift_assignments_employee_id
ON shift_assignments(employee_id);

-- Index for shift-based queries
CREATE INDEX IF NOT EXISTS idx_shift_assignments_shift_id
ON shift_assignments(shift_id);

-- Index for effective_from date queries
CREATE INDEX IF NOT EXISTS idx_shift_assignments_effective_from
ON shift_assignments(effective_from DESC);

-- Index for effective_to date queries
CREATE INDEX IF NOT EXISTS idx_shift_assignments_effective_to
ON shift_assignments(effective_to DESC);

-- Composite index for employee + date range (most common)
CREATE INDEX IF NOT EXISTS idx_shift_assignments_employee_dates
ON shift_assignments(employee_id, effective_from DESC, effective_to DESC);

-- Composite index for shift + date range
CREATE INDEX IF NOT EXISTS idx_shift_assignments_shift_dates
ON shift_assignments(shift_id, effective_from DESC, effective_to DESC);

-- =====================================================
-- ROLES TABLE INDEXES
-- =====================================================

-- Index for tenant-based role queries
CREATE INDEX IF NOT EXISTS idx_roles_tenant_id
ON roles(tenant_id);

-- Index for role name lookup
CREATE INDEX IF NOT EXISTS idx_roles_name
ON roles(name);

-- Index for system roles
CREATE INDEX IF NOT EXISTS idx_roles_is_system_role
ON roles(is_system_role);

-- =====================================================
-- USER_ROLES TABLE INDEXES
-- =====================================================

-- Index for user-based role queries
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id
ON user_roles(user_id);

-- Index for role-based user queries
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id
ON user_roles(role_id);

-- Composite index for user + role lookup
CREATE INDEX IF NOT EXISTS idx_user_roles_user_role
ON user_roles(user_id, role_id);

-- =====================================================
-- ROLE_PERMISSIONS TABLE INDEXES
-- =====================================================

-- Index for role-based permission queries
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id
ON role_permissions(role_id);

-- Index for permission-based role queries
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id
ON role_permissions(permission_id);

-- Composite index for role + permission lookup
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_permission
ON role_permissions(role_id, permission_id);

-- =====================================================
-- PERMISSIONS TABLE INDEXES
-- =====================================================

-- Index for permission code lookup (most common)
CREATE INDEX IF NOT EXISTS idx_permissions_code
ON permissions(code);

-- Index for module-based queries
CREATE INDEX IF NOT EXISTS idx_permissions_module
ON permissions(module);

-- Index for action-based queries
CREATE INDEX IF NOT EXISTS idx_permissions_action
ON permissions(action);

-- Composite index for module + action
CREATE INDEX IF NOT EXISTS idx_permissions_module_action
ON permissions(module, action);

-- =====================================================
-- SUBSCRIPTIONS TABLE INDEXES
-- =====================================================

-- Index for tenant-based subscription queries
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_id
ON subscriptions(tenant_id);

-- Index for status filtering
CREATE INDEX IF NOT EXISTS idx_subscriptions_status
ON subscriptions(status);

-- Index for plan filtering
CREATE INDEX IF NOT EXISTS idx_subscriptions_plan
ON subscriptions(plan);

-- Composite index for tenant + status (subscription checks)
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_status
ON subscriptions(tenant_id, status);

-- Index for renewal date queries
CREATE INDEX IF NOT EXISTS idx_subscriptions_next_billing_date
ON subscriptions(next_billing_date);

-- =====================================================
-- LEAVE_REQUESTS TABLE INDEXES (if exists)
-- =====================================================

-- Check if table exists before creating indexes
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables
               WHERE table_name = 'leave_requests') THEN

        CREATE INDEX IF NOT EXISTS idx_leave_requests_employee_id
        ON leave_requests(employee_id);

        CREATE INDEX IF NOT EXISTS idx_leave_requests_status
        ON leave_requests(status);

        CREATE INDEX IF NOT EXISTS idx_leave_requests_start_date
        ON leave_requests(start_date DESC);

        CREATE INDEX IF NOT EXISTS idx_leave_requests_employee_date
        ON leave_requests(employee_id, start_date DESC);
    END IF;
END $$;

-- =====================================================
-- PAYROLL TABLE INDEXES (if exists)
-- =====================================================

DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables
               WHERE table_name = 'payroll') THEN

        CREATE INDEX IF NOT EXISTS idx_payroll_employee_id
        ON payroll(employee_id);

        CREATE INDEX IF NOT EXISTS idx_payroll_tenant_id
        ON payroll(tenant_id);

        CREATE INDEX IF NOT EXISTS idx_payroll_pay_period
        ON payroll(pay_period_start, pay_period_end);

        CREATE INDEX IF NOT EXISTS idx_payroll_status
        ON payroll(status);
    END IF;
END $$;

-- =====================================================
-- PERFORMANCE ANALYSIS QUERIES
-- =====================================================

-- Run these queries after migration to verify indexes:
--
-- 1. Check all indexes:
-- SELECT tablename, indexname, indexdef
-- FROM pg_indexes
-- WHERE schemaname = 'public'
-- ORDER BY tablename, indexname;
--
-- 2. Check index usage:
-- SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
-- FROM pg_stat_user_indexes
-- ORDER BY idx_scan DESC;
--
-- 3. Check table sizes:
-- SELECT tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
-- FROM pg_tables
-- WHERE schemaname = 'public'
-- ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- =====================================================
-- EXPECTED PERFORMANCE IMPROVEMENTS
-- =====================================================
--
-- Before indexes:
-- - User login query: ~500-1000ms (full table scan)
-- - Employee list: ~800-1500ms (full table scan)
-- - Today's attendance: ~1000-2000ms (full table scan)
-- - Dashboard queries: ~2000-3000ms (multiple full scans)
--
-- After indexes:
-- - User login query: ~5-10ms (index seek)
-- - Employee list: ~10-50ms (index + filter)
-- - Today's attendance: ~10-30ms (index seek + filter)
-- - Dashboard queries: ~50-100ms (all indexed)
--
-- Expected improvement: 10-100x faster queries!
-- =====================================================
