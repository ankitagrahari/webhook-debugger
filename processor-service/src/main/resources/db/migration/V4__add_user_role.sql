-- Add role column with default USER
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Set your admin account manually
-- Replace with your actual email
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@hookspy.in';