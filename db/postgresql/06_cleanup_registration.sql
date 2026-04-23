-- =====================================================
-- CLEANUP SCRIPT
-- Run this to remove unused email_verification table
-- and unused columns from app_user
-- =====================================================

-- Drop email_verification table if exists
DROP TABLE IF EXISTS email_verification;

-- Remove unused columns from app_user (optional, comment if needed)
-- ALTER TABLE app_user DROP COLUMN IF EXISTS verified_at;