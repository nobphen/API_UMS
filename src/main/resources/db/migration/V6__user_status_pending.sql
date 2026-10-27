-- =====================================================================
-- V6__user_status_pending.sql  -  PENDING: an applicant who signed up but has not verified their email yet
-- They can't log in until they type the code sent to their email (POST /api/v1/auth/verify-email).
-- =====================================================================
ALTER TABLE users DROP CONSTRAINT users_status_check;
ALTER TABLE users ADD CONSTRAINT users_status_check CHECK (status IN ('PENDING','ACTIVE','LOCKED','DISABLED'));
