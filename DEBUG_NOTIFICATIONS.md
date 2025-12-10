# Debugging Notification Issues

## Issue: Notifications not showing after booking request

### What Should Happen:
1. User A (passenger) sends booking request for User B's (driver) ride
2. Booking is created with status "pending" ✅
3. Notification is created in database for User B (driver) ✅
4. Push notification is sent to User B's device ✅
5. User B sees notification in bell icon screen ✅

### Current Issues Fixed:

1. **Fixed notification file path** in `create_booking.php`:
   - Changed from: `../api/notifications/send_fcm_notification.php`
   - Changed to: `../notifications/send_fcm_notification.php`

2. **Fixed get_bookings.php**:
   - Changed from PDO to mysqli to match rest of codebase
   - Now properly returns booking data

3. **Updated RideDetailsActivity**:
   - Now shows "Request Pending" status immediately after sending
   - Doesn't close the screen so user can see status change

4. **Updated NotificationsActivity**:
   - Loads notifications in `onResume()` to refresh when screen opens
   - Properly displays notifications from database

## Testing Steps:

### Test 1: Send Booking Request
1. Login as User A (passenger)
2. Find a ride created by User B (driver)
3. Click "Send Booking Request"
4. Should see "Request Pending" status immediately
5. Button should change to "Request Pending"

### Test 2: Check Notifications (as Driver)
1. Login as User B (driver - the ride owner)
2. Click bell icon
3. Should see "New Booking Request" notification
4. Notification should show passenger name and seat count

### Test 3: Verify Database
1. Open phpMyAdmin
2. Check `bookings` table - should have new booking with status "pending"
3. Check `notifications` table - should have notification for driver's user_id
4. Verify `user_id` in notifications matches driver's ID

## Common Issues:

### Notifications not appearing:
- **Check**: Is the logged-in user the DRIVER (ride owner)?
- **Fix**: Notifications are created for the driver, not the passenger
- **Verify**: Check `notifications` table - `user_id` should be driver's ID

### Status not changing:
- **Check**: Is `checkExistingBooking()` being called?
- **Fix**: Status is updated in UI after successful booking creation
- **Verify**: Check `bookings` table - status should be "pending"

### Push notification not received:
- **Check**: Does driver have FCM token saved?
- **Fix**: FCM token is saved on login/registration
- **Verify**: Check `users` table - `fcm_token` should not be NULL
- **Check**: Firebase Server Key configured in `send_fcm_notification.php`

## API Endpoints to Test:

1. **Create Booking**: `POST http://192.168.18.84/campus-ride-api/bookings/create_booking.php`
   ```json
   {
     "ride_id": "ride_123",
     "passenger_id": "user_456",
     "seats_requested": "2",
     "stop_location": "",
     "status": "pending"
   }
   ```

2. **Get Notifications**: `GET http://192.168.18.84/campus-ride-api/notifications/get_notifications.php?userId=DRIVER_USER_ID`

3. **Get Bookings**: `GET http://192.168.18.84/campus-ride-api/bookings/get_bookings.php?passenger_id=USER_ID`

## Database Queries to Debug:

```sql
-- Check if booking was created
SELECT * FROM bookings ORDER BY created_at DESC LIMIT 5;

-- Check if notification was created
SELECT * FROM notifications ORDER BY created_at DESC LIMIT 5;

-- Check driver's FCM token
SELECT id, name, fcm_token FROM users WHERE id = 'DRIVER_USER_ID';

-- Check booking status
SELECT b.*, r.driver_id, r.driver_name 
FROM bookings b 
LEFT JOIN rides r ON b.ride_id = r.id 
WHERE b.passenger_id = 'PASSENGER_USER_ID';
```

## Files Updated:

1. ✅ `backend/bookings/create_booking.php` - Fixed notification path
2. ✅ `backend/bookings/get_bookings.php` - Fixed to use mysqli
3. ✅ `app/src/main/java/com/example/campusride/RideDetailsActivity.kt` - Shows status immediately
4. ✅ `app/src/main/java/com/example/campusride/NotificationsActivity.kt` - Refreshes on resume
5. ✅ All files copied to XAMPP

## Next Steps:

1. **Test as Driver**: Login as the ride owner and check notifications
2. **Verify Database**: Check that notifications are being created
3. **Check FCM Token**: Ensure driver has FCM token saved
4. **Test Push Notification**: Verify Firebase Server Key is configured

