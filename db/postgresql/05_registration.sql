-- =====================================================
-- REGISTRATION MODULE
-- Public registration and user verification
-- Run AFTER 01_schema_academic.sql
-- =====================================================

-- =====================================================
-- 1. REGISTRATION REQUEST
-- Stores public registration requests
-- =====================================================

CREATE TABLE registration_request (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    curp VARCHAR(18) NOT NULL,
    email VARCHAR(150) NOT NULL,
    
    student_id UUID REFERENCES student(id) ON DELETE SET NULL,
    teacher_id UUID REFERENCES teacher(id) ON DELETE SET NULL,
    
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED')),
    
    otp_code VARCHAR(6) NOT NULL,
    otp_expires_at TIMESTAMPTZ NOT NULL,
    
    requested_at TIMESTAMPTZ DEFAULT now(),
    processed_at TIMESTAMPTZ,
    processed_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    
    rejection_reason TEXT,
    notes TEXT,
    
    UNIQUE (curp)
);

CREATE INDEX idx_registration_request_curp ON registration_request(curp);
CREATE INDEX idx_registration_request_status ON registration_request(status);
CREATE INDEX idx_registration_request_email ON registration_request(email);

-- =====================================================
-- 2. USER STATUS
-- Extended user status for academic system
-- =====================================================

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS temp_password VARCHAR(255);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS must_verify_email BOOLEAN DEFAULT FALSE;

-- =====================================================
-- 3. SYSTEM CONFIGURATION - Registration Settings
-- =====================================================

INSERT INTO system_configuration (config_key, config_value, description, data_type, module) VALUES
('REGISTRATION_OTP_EXPIRY_MINUTES', '30', 'OTP code expiry in minutes', 'NUMBER', 'REGISTRATION'),
('REGISTRATION_REQUIRES_ACADEMIC_RECORD', 'true', 'Registration requires valid student/teacher record', 'BOOLEAN', 'REGISTRATION'),
('USER_MAX_ROLES_WITH_CURP', '2', 'Maximum roles for users with academic record', 'NUMBER', 'REGISTRATION'),
('USER_MAX_ROLES_WITHOUT_CURP', '1', 'Maximum roles for system users', 'NUMBER', 'REGISTRATION');