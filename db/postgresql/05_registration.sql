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
-- 2. EMAIL VERIFICATION
-- Stores email verification codes
-- =====================================================

CREATE TABLE email_verification (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    
    verification_code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_email_verification_user ON email_verification(user_id);
CREATE INDEX idx_email_verification_code ON email_verification(verification_code);

-- =====================================================
-- 3. USER STATUS
-- Extended user status for academic system
-- =====================================================

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS verified_at TIMESTAMPTZ;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS temp_password VARCHAR(255);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS must_verify_email BOOLEAN DEFAULT FALSE;

-- =====================================================
-- 4. SYSTEM CONFIGURATION - Registration Settings
-- =====================================================

INSERT INTO system_configuration (config_key, config_value, description, data_type, module) VALUES
('REGISTRATION_OTP_EXPIRY_MINUTES', '30', 'OTP code expiry in minutes', 'NUMBER', 'REGISTRATION'),
('EMAIL_VERIFICATION_OTP_EXPIRY_MINUTES', '30', 'Email verification OTP expiry', 'NUMBER', 'REGISTRATION'),
('REGISTRATION_REQUIRES_ACADEMIC_RECORD', 'true', 'Registration requires valid student/teacher record', 'BOOLEAN', 'REGISTRATION'),
('ALLOW_STAFF_REGISTRATION', 'true', 'Allow admin to create staff users without academic record', 'BOOLEAN', 'REGISTRATION');