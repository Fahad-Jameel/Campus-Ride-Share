# Notification System Setup Guide

## Overview
The notification system sends push notifications to ride owners when booking requests are made. Notifications are stored in the database and displayed in the NotificationsActivity (bell icon).

## Database Setup

1. **Run the database migration**:
   ```sql
   source backend/api/database_notifications.sql
   ```
   
   This will:
   - Add `fcm_token` column to `users` table
   - Create `notifications` table
   - Create `bookings` table (if it doesn't exist)

## Firebase Setup

1. **Get Firebase Server Key**:
   - Go to Firebase Console (https://console.firebase.google.com/)
   - Select your project
   - Go to Project Settings > Cloud Messaging
   - Copy the "Server key"

2. **Update FCM Server Key**:
   - Edit `backend/api/notifications/send_fcm_notification.php`
   - Replace `YOUR_FIREBASE_SERVER_KEY_HERE` with your actual server key:
   ```php
   $serverKey = "your-actual-server-key-here";
   ```

## How It Works

### When a booking request is created:

1. **Booking Creation** (`backend/bookings/create_booking.php`):
   - Creates booking record in database
   - Gets driver's FCM token from `users` table
   - Sends push notification via FCM
   - Creates notification record in `notifications` table

2. **Push Notification**:
   - Sent to driver's device via Firebase Cloud Messaging
   - Shows in system notification tray
   - Opens ManageBookingsActivity when tapped

3. **Notification Storage**:
   - Saved in `notifications` table
   - Visible in NotificationsActivity (bell icon)
   - Marked as read when user views it

### Notification Flow:

```
User sends booking request
    ↓
create_booking.php creates booking
    ↓
Gets driver's FCM token
    ↓
Sends FCM push notification
    ↓
Creates notification record in database
    ↓
Driver receives push notification
    ↓
Driver opens app → sees notification in bell icon
```

## Testing

1. **Test Booking Request**:
   - User A creates a ride
   - User B requests to book that ride
   - User A should receive:
     - Push notification on device
     - Notification in NotificationsActivity

2. **Test Notification Display**:
   - Open app as driver
   - Click bell icon
   - Should see booking request notification
   - Click notification → opens ManageBookingsActivity

## Troubleshooting

### Notifications not appearing:

1. **Check FCM Token**:
   - Ensure user has logged in (FCM token is saved on login)
   - Check `users.fcm_token` in database

2. **Check Firebase Server Key**:
   - Verify server key is correct in `send_fcm_notification.php`
   - Check Firebase Console for errors

3. **Check Database**:
   - Verify `notifications` table exists
   - Check if notification records are being created

4. **Check API Endpoints**:
   - Verify `get_notifications.php` is accessible
   - Check API response in logs

### Push notifications not working:

1. **Firebase Setup**:
   - Ensure `google-services.json` is in `app/` directory
   - Verify Firebase project is configured correctly

2. **Device Permissions**:
   - Check notification permissions are granted
   - Android 13+ requires POST_NOTIFICATIONS permission

3. **FCM Token**:
   - Token is generated on first app launch
   - Saved to server on login
   - Check `MyFirebaseMessagingService` logs

## API Endpoints

- `POST /api/users/save_fcm_token.php` - Save FCM token
- `GET /api/notifications/get_notifications.php?userId={userId}` - Get notifications
- `POST /api/notifications/mark_read.php` - Mark notification as read

## Files Modified/Created

### Backend:
- `backend/api/database_notifications.sql` - Database schema
- `backend/api/notifications/send_fcm_notification.php` - FCM notification sender
- `backend/api/notifications/get_notifications.php` - Get notifications endpoint
- `backend/api/notifications/mark_read.php` - Mark as read endpoint
- `backend/api/users/save_fcm_token.php` - Save FCM token endpoint
- `backend/bookings/create_booking.php` - Updated to send notifications

### Android:
- `app/src/main/java/com/example/campusride/NotificationsActivity.kt` - Display notifications
- `app/src/main/java/com/example/campusride/NotificationAdapter.kt` - Notification list adapter
- `app/src/main/java/com/example/campusride/data/repository/NotificationRepository.kt` - Notification repository
- `app/src/main/java/com/example/campusride/data/api/ApiService.kt` - Added notification endpoints
- `app/src/main/java/com/example/campusride/MyFirebaseMessagingService.kt` - Updated to save FCM token
- `app/src/main/java/com/example/campusride/LoginActivity.kt` - Send FCM token on login
- `app/src/main/java/com/example/campusride/CreateAccountActivity.kt` - Send FCM token on registration
- `app/src/main/res/layout/item_notification.xml` - Notification item layout

