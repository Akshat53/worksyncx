-- V4: Create plans table for subscription plan management

CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    short_description VARCHAR(200),
    monthly_price DECIMAL(12, 2) NOT NULL,
    yearly_price DECIMAL(12, 2) NOT NULL,
    max_employees INTEGER,
    modules JSONB,
    features JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_popular BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER,
    badge_text VARCHAR(50),
    badge_color VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create indexes for better query performance
CREATE INDEX idx_plans_is_active ON plans(is_active);
CREATE INDEX idx_plans_display_order ON plans(display_order);
CREATE INDEX idx_plans_name ON plans(name);

-- Insert default plans
INSERT INTO plans (name, short_description, description, monthly_price, yearly_price, max_employees, modules, features, is_active, is_popular, display_order, badge_text, badge_color) VALUES
('Starter', 'Perfect for small teams', 'Ideal for small businesses just getting started with HRMS', 29.99, 299.99, 25,
 '["DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES"]'::jsonb,
 '["Employee Database", "Department Management", "Designation Management", "Basic Reporting"]'::jsonb,
 true, false, 1, null, null),

('Professional', 'Best for growing companies', 'Comprehensive HR solution for medium-sized businesses', 79.99, 799.99, 100,
 '["DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES", "ATTENDANCE", "LEAVE_MANAGEMENT"]'::jsonb,
 '["Everything in Starter", "Attendance Tracking", "Leave Management", "Advanced Reporting", "Email Support"]'::jsonb,
 true, true, 2, 'Most Popular', '#8b5cf6'),

('Enterprise', 'For large organizations', 'Full-featured HRMS with advanced capabilities and dedicated support', 149.99, 1499.99, null,
 '["DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES", "ATTENDANCE", "LEAVE_MANAGEMENT", "PAYROLL", "REPORTS"]'::jsonb,
 '["Everything in Professional", "Payroll Management", "Advanced Analytics", "Custom Integrations", "Priority Support", "Dedicated Account Manager", "Unlimited Employees"]'::jsonb,
 true, false, 3, 'Enterprise', '#3b82f6');
