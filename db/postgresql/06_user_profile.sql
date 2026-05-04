-- =====================================================
-- USER PROFILE TABLE
-- =====================================================

CREATE TABLE user_profile (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
    
    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    curp VARCHAR(18) UNIQUE,
    rfc VARCHAR(13) UNIQUE,
    phone VARCHAR(20),
    secondary_phone VARCHAR(20),
    birth_date DATE,
    gender VARCHAR(1) CHECK (gender IN ('M', 'F', 'O')),
    
    -- Academic/Professional
    employee_number VARCHAR(20) UNIQUE,
    enrollment_number VARCHAR(20) UNIQUE,
    institutional_email VARCHAR(150) UNIQUE,
    secondary_email VARCHAR(150),
    
    -- Address (optional)
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(10),
    
    -- Profile Picture
    profile_picture_url VARCHAR(500),
    
    -- Metadata
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_user_profile_user ON user_profile(user_id);
CREATE INDEX idx_user_profile_curp ON user_profile(curp);
CREATE INDEX idx_user_profile_employee ON user_profile(employee_number);
CREATE INDEX idx_user_profile_enrollment ON user_profile(enrollment_number);
