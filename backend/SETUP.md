# Backend Setup Guide

## Prerequisites

Before you begin, ensure you have the following installed:

1. **PHP** (version 7.4 or higher)
   - Check: `php --version`
2. **MySQL** (version 5.7 or higher) or **MariaDB**
   - Check: `mysql --version`
3. **Web Server** - Choose one:
   - **XAMPP** (recommended for beginners - includes PHP, MySQL, Apache)
   - **MAMP** (for macOS)
   - **Apache** with PHP module
   - **PHP Built-in Server** (for development only)

---

## Option 1: Using XAMPP (Recommended for Development)

### Step 1: Install XAMPP
1. Download XAMPP from [https://www.apachefriends.org/](https://www.apachefriends.org/)
2. Install and launch XAMPP Control Panel
3. Start **Apache** and **MySQL** services

### Step 2: Setup Project Files
```bash
# Copy the backend/api folder to XAMPP's htdocs directory
# On Mac:
cp -r backend/api /Applications/XAMPP/htdocs/campus-ride-api

# On Windows:
# Copy backend/api to C:\xampp\htdocs\campus-ride-api
```

### Step 3: Create Database
1. Open phpMyAdmin: [http://localhost/phpmyadmin](http://localhost/phpmyadmin)
2. Click "New" to create a database
3. Name it: **`campus_ride`**
4. Select Collation: **`utf8mb4_general_ci`**
5. Click "Create"

### Step 4: Import Database Schema
1. In phpMyAdmin, select the `campus_ride` database
2. Click the "SQL" tab
3. Copy and paste the contents from `backend/api/database.sql`
4. OR click "Import" → Choose `database.sql` file → Click "Go"

### Step 5: Configure Database Connection
Edit `backend/api/config.php`:
```php
<?php
define('DB_HOST', 'localhost');
define('DB_NAME', 'campus_ride');
define('DB_USER', 'root');          // Default XAMPP username
define('DB_PASS', '');              // Default XAMPP password is empty
define('DB_CHARSET', 'utf8mb4');
```

### Step 6: Test Backend
Open your browser and test:
```
http://localhost/campus-ride-api/auth/register.php
```

You should see a JSON response (likely an error because no POST data, but that's okay - it means the endpoint is working!)

---

## Option 2: Using PHP Built-in Server (Quick Test)

### Step 1: Navigate to API Directory
```bash
cd backend/api
```

### Step 2: Setup MySQL Database
```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE campus_ride CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# Import schema
USE campus_ride;
SOURCE database.sql;

# Exit MySQL
exit;
```

### Step 3: Configure Database Connection
Edit `config.php` with your MySQL credentials:
```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'campus_ride');
define('DB_USER', 'your_mysql_username');
define('DB_PASS', 'your_mysql_password');
```

### Step 4: Start PHP Server
```bash
# Run from backend/api directory
php -S localhost:8000
```

### Step 5: Test Endpoints
```bash
# Test registration endpoint
curl -X POST http://localhost:8000/auth/register.php \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }'
```

---

## Option 3: Using MAMP (for macOS)

### Step 1: Install MAMP
1. Download MAMP from [https://www.mamp.info/](https://www.mamp.info/)
2. Install and launch MAMP
3. Start servers

### Step 2: Setup Files
```bash
# Copy backend/api to MAMP's htdocs
cp -r backend/api /Applications/MAMP/htdocs/campus-ride-api
```

### Step 3: Create Database
1. Open phpMyAdmin: [http://localhost:8888/phpMyAdmin](http://localhost:8888/phpMyAdmin)
2. Create database: `campus_ride`
3. Import `database.sql`

### Step 4: Configure Database
Edit `config.php`:
```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'campus_ride');
define('DB_USER', 'root');
define('DB_PASS', 'root');  // MAMP default password
```

---

## Updating Android App to Use Backend

Once your backend is running, update the Android app's API base URL:

### Edit: `app/src/main/java/com/example/campusride/data/api/RetrofitClient.kt`

```kotlin
object RetrofitClient {
    // Update this URL based on your setup:
    
    // For XAMPP/MAMP:
    private const val BASE_URL = "http://10.0.2.2/campus-ride-api/"  // Android Emulator
    // OR
    private const val BASE_URL = "http://YOUR_COMPUTER_IP/campus-ride-api/"  // Real Device
    
    // For PHP Built-in Server:
    private const val BASE_URL = "http://10.0.2.2:8000/"  // Android Emulator
    
    // ... rest of the code
}
```

### Finding Your Computer's IP Address:
```bash
# On macOS/Linux:
ifconfig | grep "inet "

# On Windows:
ipconfig
```

Look for your local network IP (usually starts with 192.168.x.x)

---

## Testing API Endpoints

### Using cURL (Command Line)

**Register User:**
```bash
curl -X POST http://localhost/campus-ride-api/auth/register.php \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123",
    "name": "John Doe",
    "phone": "03001234567"
  }'
```

**Login:**
```bash
curl -X POST http://localhost/campus-ride-api/auth/login.php \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Get Rides:**
```bash
curl http://localhost/campus-ride-api/rides/get_rides.php
```

### Using Postman (Graphical Tool)

1. Download Postman: [https://www.postman.com/downloads/](https://www.postman.com/downloads/)
2. Create a new request
3. Set method to POST/GET
4. Enter URL: `http://localhost/campus-ride-api/auth/register.php`
5. Set Headers: `Content-Type: application/json`
6. Add JSON body
7. Click Send

---

## Common Issues & Solutions

### 1. "Access denied for user"
**Solution:** Check MySQL username/password in `config.php`

### 2. "Cannot connect to MySQL"
**Solution:** 
- Ensure MySQL service is running
- Check if MySQL port (3306) is correct
- Try `localhost` or `127.0.0.1`

### 3. "404 Not Found"
**Solution:**
- Check file path matches URL
- Verify Apache/MAMP is running
- Check `.htaccess` if using mod_rewrite

### 4. Android app can't connect
**Solution:**
- Use `10.0.2.2` instead of `localhost` for Android Emulator
- Use your computer's IP address for real devices
- Ensure CORS is configured (already handled in backend files)
- Check firewall settings

### 5. "Fatal error: Call to undefined function"
**Solution:** Enable required PHP extensions in `php.ini`:
```ini
extension=mysqli
extension=pdo_mysql
extension=mbstring
```

---

## Production Deployment

For production, consider:

1. **Use a proper web server** (Apache/Nginx)
2. **Enable HTTPS** with SSL certificate
3. **Update `config.php`** with production credentials
4. **Set proper file permissions**
5. **Enable error logging** (disable error display)
6. **Use environment variables** for sensitive data
7. **Add rate limiting** to prevent abuse

---

## Next Steps

1. ✅ Start your web server (XAMPP/MAMP/PHP server)
2. ✅ Create and import database
3. ✅ Configure `config.php`
4. ✅ Test endpoints with cURL or Postman
5. ✅ Update Android app's `BASE_URL`
6. ✅ Setup Firebase Realtime Database (see Firebase setup below)
7. ✅ Run the Android app!

---

## Firebase Setup (Required for Images)

Your app uses Firebase Realtime Database for image storage. Follow these steps:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Add an Android app:
   - Package name: `com.example.campusride`
   - Download `google-services.json`
   - Place it in `app/` directory (you already have this)
4. Enable **Realtime Database**:
   - Go to Realtime Database in Firebase Console
   - Click "Create Database"
   - Start in **test mode** (for development)
5. Update security rules later for production:
```json
{
  "rules": {
    "images": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

---

## Support

If you encounter issues:
1. Check the error logs (`/var/log/apache2/error.log` or XAMPP logs)
2. Test individual endpoints with Postman
3. Verify database tables were created correctly
4. Check Android app's Logcat for network errors

Happy coding! 🚀
