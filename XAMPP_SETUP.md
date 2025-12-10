# XAMPP Setup Guide for Campus RideShare Backend

## Step 1: Install XAMPP

1. Download XAMPP from: https://www.apachefriends.org/
2. Install XAMPP on your Windows/Mac/Linux machine
3. Start Apache and MySQL from XAMPP Control Panel

## Step 2: Copy Backend Files to XAMPP

### For Windows:
1. Navigate to: `C:\xampp\htdocs\`
2. Create a folder named: `campus-ride-api`
3. Copy all files from `backend/api/` to `C:\xampp\htdocs\campus-ride-api\`

### For Mac:
1. Navigate to: `/Applications/XAMPP/htdocs/`
2. Create a folder named: `campus-ride-api`
3. Copy all files from `backend/api/` to `/Applications/XAMPP/htdocs/campus-ride-api/`

### For Linux:
1. Navigate to: `/opt/lampp/htdocs/`
2. Create a folder named: `campus-ride-api`
3. Copy all files from `backend/api/` to `/opt/lampp/htdocs/campus-ride-api/`

## Step 3: Setup Database

1. Open phpMyAdmin: http://localhost/phpmyadmin
2. Click on "Import" tab
3. Select `backend/api/database.sql` file
4. Click "Go" to import
5. Also run `backend/api/database_notifications.sql` to add notifications table

## Step 4: Configure Database Connection

1. Edit `C:\xampp\htdocs\campus-ride-api\config.php` (or equivalent path)
2. Update database credentials:
   ```php
   define('DB_HOST', 'localhost');
   define('DB_USER', 'root');
   define('DB_PASS', ''); // Usually empty for XAMPP
   define('DB_NAME', 'campus_rideshare');
   ```

## Step 5: Set Permissions (Mac/Linux)

```bash
cd /Applications/XAMPP/htdocs/campus-ride-api
chmod -R 755 uploads/
chmod -R 755 ../campus-ride-api/uploads/
```

## Step 6: Create Uploads Directory

1. Navigate to `campus-ride-api` folder
2. Create `uploads` folder
3. Inside `uploads`, create subfolders:
   - `profile`
   - `vehicle`
   - `ride`
   - `general`

## Step 7: Test API

1. Open browser
2. Go to: `http://localhost/campus-ride-api/config.php`
3. Should see JSON response (or no error)

## Step 8: Update Android App Base URL

1. Find your computer's IP address:
   - Windows: Open CMD, type `ipconfig`, look for IPv4 Address
   - Mac/Linux: Open Terminal, type `ifconfig`, look for inet address
   
2. Update `app/src/main/java/com/example/campusride/data/api/RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://YOUR_IP_ADDRESS/campus-ride-api/"
   ```
   Example: `http://192.168.18.84/campus-ride-api/`

## Step 9: Configure Firebase Server Key

1. Edit `C:\xampp\htdocs\campus-ride-api\notifications\send_fcm_notification.php`
2. Replace `YOUR_FIREBASE_SERVER_KEY_HERE` with your Firebase Server Key

## Step 10: Enable Apache Modules (if needed)

1. Open XAMPP Control Panel
2. Click "Config" next to Apache
3. Select "httpd.conf"
4. Make sure these are uncommented:
   ```
   LoadModule rewrite_module modules/mod_rewrite.so
   LoadModule headers_module modules/mod_headers.so
   ```

## Troubleshooting

### API returns 404:
- Check if Apache is running
- Verify files are in correct location
- Check URL path matches folder name

### Database connection error:
- Ensure MySQL is running in XAMPP
- Check database credentials in config.php
- Verify database exists in phpMyAdmin

### Permission denied errors:
- Check file/folder permissions
- Ensure Apache has read access

### CORS errors:
- Already handled in config.php with headers
- If still issues, check Apache mod_headers is enabled

## File Structure in XAMPP

```
htdocs/
└── campus-ride-api/
    ├── config.php
    ├── database.sql
    ├── database_notifications.sql
    ├── auth/
    │   ├── register.php
    │   └── login.php
    ├── users/
    │   ├── get_user.php
    │   ├── update_user.php
    │   └── save_fcm_token.php
    ├── rides/
    │   ├── get_rides.php
    │   └── create_ride.php
    ├── vehicles/
    │   ├── get_vehicles.php
    │   ├── create_vehicle.php
    │   └── update_vehicle.php
    ├── images/
    │   └── upload.php
    ├── chats/
    │   └── get_chats.php
    ├── messages/
    │   ├── get_messages.php
    │   └── send_message.php
    ├── notifications/
    │   ├── send_fcm_notification.php
    │   ├── get_notifications.php
    │   └── mark_read.php
    ├── bookings/
    │   ├── create_booking.php
    │   ├── get_bookings.php
    │   └── update_booking.php
    └── uploads/
        ├── profile/
        ├── vehicle/
        ├── ride/
        └── general/
```

## Quick Test Commands

Test registration endpoint:
```bash
curl -X POST http://localhost/campus-ride-api/auth/register.php \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123","name":"Test User"}'
```

Test login endpoint:
```bash
curl -X POST http://localhost/campus-ride-api/auth/login.php \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'
```

## Notes

- Keep XAMPP running while testing the app
- Use your computer's local IP (not localhost) for mobile device access
- Ensure phone and computer are on same WiFi network
- For production, use HTTPS and proper domain

