# Campus RideShare PHP Backend API

## Setup Instructions

1. **Database Setup:**
   - Import `database.sql` into your MySQL database
   - Update database credentials in `config.php`

2. **Server Configuration:**
   - Place the `api` folder in your web server directory (e.g., `htdocs` or `www`)
   - Ensure PHP 7.4+ is installed
   - Enable MySQL extension in PHP

3. **File Permissions:**
   - Create `uploads` directory with write permissions:
     ```bash
     mkdir -p uploads/profile uploads/vehicle uploads/ride uploads/general
     chmod 777 uploads -R
     ```

4. **Base URL Configuration:**
   - Update the base URL in `images/upload.php` to match your server
   - Update API base URL in Android app's `ApiService.kt`

## API Endpoints

### Authentication
- `POST /auth/register.php` - Register new user
- `POST /auth/login.php` - Login user

### Users
- `GET /users/get_user.php?id={userId}` - Get user by ID
- `PUT /users/update_user.php` - Update user profile

### Rides
- `GET /rides/get_rides.php?pickup={location}&destination={location}&date={date}&search={query}` - Get rides with filters
- `POST /rides/create_ride.php` - Create new ride

### Vehicles
- `GET /vehicles/get_vehicles.php?userId={userId}` - Get user's vehicles
- `POST /vehicles/create_vehicle.php` - Add vehicle
- `PUT /vehicles/update_vehicle.php` - Update vehicle

### Images
- `POST /images/upload.php` - Upload image (multipart/form-data)

### Chats
- `GET /chats/get_chats.php?userId={userId}` - Get user's chats

### Messages
- `GET /messages/get_messages.php?chatId={chatId}` - Get chat messages
- `POST /messages/send_message.php` - Send message

## Request/Response Format

All requests (except file uploads) use JSON format.
All responses are in JSON format with `success` and `data`/`error` fields.

Example Response:
```json
{
  "success": true,
  "user": {
    "id": "user_123",
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```


