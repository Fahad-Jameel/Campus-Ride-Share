# Campus RideShare - Setup Guide

## Quick Start

### 1. Backend Setup (PHP/MySQL)

1. **Install Requirements**:
   - PHP 7.4 or higher
   - MySQL 5.7 or higher
   - Apache/Nginx web server

2. **Database Setup**:
   ```bash
   # Login to MySQL
   mysql -u root -p
   
   # Create database and import schema
   source backend/api/database.sql
   ```

3. **Configure Database**:
   Edit `backend/api/config.php`:
   ```php
   define('DB_HOST', 'localhost');
   define('DB_USER', 'your_username');
   define('DB_PASS', 'your_password');
   define('DB_NAME', 'campus_rideshare');
   ```

4. **Setup Upload Directory**:
   ```bash
   cd backend/api
   mkdir -p uploads/profile uploads/vehicle uploads/ride uploads/general
   chmod 777 uploads -R
   ```

5. **Update Base URL**:
   Edit `backend/api/images/upload.php`:
   ```php
   $baseUrl = 'http://your-domain.com/api/uploads/';
   ```

6. **Deploy to Web Server**:
   - Copy `backend/api/` folder to your web server directory
   - Ensure PHP has write permissions to `uploads/` directory

### 2. Android Setup

1. **Update API Base URL**:
   Edit `app/src/main/java/com/example/campusride/data/api/RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://your-server.com/api/"
   ```

2. **Build and Run**:
   - Open project in Android Studio
   - Sync Gradle files
   - Build and run on device/emulator

### 3. Testing

1. **Test Registration**:
   - Open app
   - Click "Create Account"
   - Fill in details and register
   - Should navigate to Home screen

2. **Test Login**:
   - Use registered credentials
   - Should authenticate and navigate to Home

3. **Test Data Sync**:
   - Create a ride
   - Check MySQL database to verify data is saved
   - Check Room database (using Database Inspector in Android Studio)

## API Endpoints

### Authentication
- `POST /api/auth/register.php` - Register new user
- `POST /api/auth/login.php` - Login user

### Users
- `GET /api/users/get_user.php?id={userId}` - Get user
- `PUT /api/users/update_user.php` - Update user

### Rides
- `GET /api/rides/get_rides.php?pickup={location}&destination={location}&date={date}&search={query}` - Search rides
- `POST /api/rides/create_ride.php` - Create ride

### Vehicles
- `GET /api/vehicles/get_vehicles.php?userId={userId}` - Get vehicles
- `POST /api/vehicles/create_vehicle.php` - Add vehicle
- `PUT /api/vehicles/update_vehicle.php` - Update vehicle

### Images
- `POST /api/images/upload.php` - Upload image (multipart/form-data)

### Chats & Messages
- `GET /api/chats/get_chats.php?userId={userId}` - Get chats
- `GET /api/messages/get_messages.php?chatId={chatId}` - Get messages
- `POST /api/messages/send_message.php` - Send message

## Troubleshooting

### Backend Issues

1. **Database Connection Error**:
   - Check MySQL is running
   - Verify credentials in `config.php`
   - Ensure database exists

2. **Image Upload Fails**:
   - Check `uploads/` directory permissions
   - Verify PHP `upload_max_filesize` setting
   - Check web server write permissions

3. **CORS Errors**:
   - Headers are already set in `config.php`
   - Ensure web server allows CORS

### Android Issues

1. **Network Error**:
   - Check internet permission in manifest
   - Verify API base URL is correct
   - Check device/emulator can reach server

2. **Build Errors**:
   - Sync Gradle files
   - Clean and rebuild project
   - Check all dependencies are downloaded

3. **Database Errors**:
   - Clear app data and reinstall
   - Check Room database version matches

## Security Notes

1. **Production Deployment**:
   - Use HTTPS for API calls
   - Implement proper JWT token authentication
   - Add rate limiting
   - Sanitize all inputs
   - Use prepared statements (already implemented)

2. **Password Security**:
   - Passwords are hashed using `password_hash()` (bcrypt)
   - Never store plain text passwords

3. **API Security**:
   - Add authentication tokens to API requests
   - Implement request signing
   - Add API key validation

## Next Steps

1. **Complete Activity Integration**:
   - Connect all activities to repositories
   - Add loading states
   - Add error handling

2. **Add Features**:
   - Push notifications
   - Real-time chat (WebSocket)
   - Ride booking functionality
   - Payment integration

3. **Testing**:
   - Unit tests for repositories
   - Integration tests for API
   - UI tests for activities

## Support

For issues or questions, refer to:
- `IMPLEMENTATION_SUMMARY.md` - Feature documentation
- `backend/api/README.md` - API documentation
- Code comments in repository classes

