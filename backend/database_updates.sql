-- Add fcm_token column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(255) DEFAULT NULL;
CREATE INDEX IF NOT EXISTS idx_fcm_token ON users(fcm_token);

-- Add rejection_reason column to bookings table
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS rejection_reason TEXT DEFAULT NULL;

-- Check tables
SELECT 'Users table' as table_name;
DESCRIBE users;

SELECT 'Bookings table' as table_name;
DESCRIBE bookings;
