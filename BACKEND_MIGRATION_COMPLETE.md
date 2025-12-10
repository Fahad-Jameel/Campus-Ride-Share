# Backend Migration to XAMPP - COMPLETE ✅

## Migration Status

✅ **Backend files copied to XAMPP**
- Location: `/Applications/XAMPP/htdocs/campus-ride-api/`
- All PHP files copied
- Uploads directories created

## Next Steps

### 1. Start XAMPP Services
1. Open XAMPP Control Panel
2. Start **Apache** server
3. Start **MySQL** server

### 2. Import Database

1. Open phpMyAdmin: http://localhost/phpmyadmin
2. Click on **"Import"** tab
3. Click **"Choose File"** and select:
   - `backend/api/database.sql`
   - Click **"Go"** to import
4. Repeat for:
   - `backend/api/database_notifications.sql`
   - Click **"Go"** to import

### 3. Verify Database Configuration

The `config.php` file is already configured for XAMPP:
```php
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', ''); // Empty for XAMPP default
define('DB_NAME', 'campus_rideshare');
```

### 4. Test API Endpoints

Open in browser to test:
- http://localhost/campus-ride-api/config.php
- http://localhost/campus-ride-api/auth/login.php

### 5. Configure Firebase Server Key

1. Edit: `/Applications/XAMPP/htdocs/campus-ride-api/notifications/send_fcm_notification.php`
2. Replace `YOUR_FIREBASE_SERVER_KEY_HERE` with your actual Firebase Server Key
3. Get key from: Firebase Console > Project Settings > Cloud Messaging > Server Key

### 6. Verify Uploads Directory Permissions

Uploads directory should have write permissions:
```bash
chmod -R 755 /Applications/XAMPP/htdocs/campus-ride-api/uploads
```

## File Structure in XAMPP

```
/Applications/XAMPP/htdocs/campus-ride-api/
├── config.php
├── database.sql
├── database_notifications.sql
├── README.md
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

## API Base URL

Your Android app is configured to use:
```
http://192.168.18.84/campus-ride-api/
```

This will work when:
- XAMPP Apache is running
- Your phone and computer are on the same WiFi network
- Your computer's IP is 192.168.18.84

## Testing Checklist

- [ ] XAMPP Apache is running
- [ ] XAMPP MySQL is running
- [ ] Database imported successfully
- [ ] Can access http://localhost/campus-ride-api/config.php
- [ ] Uploads directory has write permissions
- [ ] Firebase Server Key configured
- [ ] Test booking request from Android app

## Troubleshooting

### Cannot access API from phone:
- Check computer's IP address: `ifconfig | grep inet`
- Update `RetrofitClient.kt` with correct IP
- Ensure phone and computer are on same WiFi

### Database connection error:
- Check MySQL is running in XAMPP
- Verify database name is `campus_rideshare`
- Check credentials in config.php

### Permission errors:
```bash
sudo chmod -R 755 /Applications/XAMPP/htdocs/campus-ride-api
sudo chown -R $(whoami):staff /Applications/XAMPP/htdocs/campus-ride-api
```

## Migration Complete! 🎉

Your backend is now running on XAMPP. You can test the booking request feature from your Android app.

