# Migrating Backend to XAMPP

## Quick Migration Steps

### 1. Copy Files to XAMPP

**Windows:**
```bash
# Copy all backend files to XAMPP htdocs
xcopy /E /I backend\api C:\xampp\htdocs\campus-ride-api
```

**Mac/Linux:**
```bash
# Copy all backend files to XAMPP htdocs
cp -r backend/api/* /Applications/XAMPP/htdocs/campus-ride-api/
# OR for Linux:
cp -r backend/api/* /opt/lampp/htdocs/campus-ride-api/
```

### 2. Create Required Folders

Navigate to `campus-ride-api` folder and create:
```
uploads/
├── profile/
├── vehicle/
├── ride/
└── general/
```

### 3. Import Database

1. Open phpMyAdmin: http://localhost/phpmyadmin
2. Click "Import" tab
3. Select `database.sql` file
4. Click "Go"
5. Repeat for `database_notifications.sql`

### 4. Update config.php

Edit `C:\xampp\htdocs\campus-ride-api\config.php`:
```php
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', ''); // Usually empty for XAMPP
define('DB_NAME', 'campus_rideshare');
```

### 5. Update Android App URL

The URL should be:
```
http://YOUR_IP_ADDRESS/campus-ride-api/
```

Example: `http://192.168.18.84/campus-ride-api/`

### 6. Test API

Open browser and test:
- http://localhost/campus-ride-api/config.php
- Should return JSON or no error

### 7. Update Firebase Server Key

Edit `C:\xampp\htdocs\campus-ride-api\notifications\send_fcm_notification.php`:
```php
$serverKey = "YOUR_ACTUAL_FIREBASE_SERVER_KEY";
```

## File Structure After Migration

```
C:\xampp\htdocs\campus-ride-api\
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

## Verify Everything Works

1. **Start XAMPP**: Apache and MySQL should be running
2. **Test in Browser**: http://localhost/campus-ride-api/config.php
3. **Test from Phone**: Use your computer's IP address
4. **Check Database**: Open phpMyAdmin and verify tables exist

## Common Issues

### 404 Not Found
- Check Apache is running
- Verify folder name is `campus-ride-api`
- Check file paths are correct

### Database Connection Error
- Ensure MySQL is running
- Check credentials in config.php
- Verify database exists

### Permission Errors
- Set folder permissions to 755
- Ensure Apache can read files

### CORS Errors
- Already handled in config.php
- Check Apache mod_headers is enabled

