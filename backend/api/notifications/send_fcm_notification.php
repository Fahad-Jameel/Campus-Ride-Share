<?php
/**
 * Send FCM push notification
 * Requires Firebase Server Key
 */

function sendFCMNotification($fcmToken, $title, $body, $data = []) {
    // TODO: Replace with your Firebase Server Key from Firebase Console
    // Go to Firebase Console > Project Settings > Cloud Messaging > Server Key
    $serverKey = "YOUR_FIREBASE_SERVER_KEY_HERE";
    
    if ($serverKey === "YOUR_FIREBASE_SERVER_KEY_HERE") {
        error_log("FCM Server Key not configured");
        return false;
    }
    
    $url = 'https://fcm.googleapis.com/fcm/send';
    
    $notification = [
        'title' => $title,
        'body' => $body,
        'sound' => 'default',
        'badge' => '1'
    ];
    
    $fields = [
        'to' => $fcmToken,
        'notification' => $notification,
        'data' => $data,
        'priority' => 'high'
    ];
    
    $headers = [
        'Authorization: key=' . $serverKey,
        'Content-Type: application/json'
    ];
    
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
    
    $result = curl_exec($ch);
    curl_close($ch);
    
    $response = json_decode($result, true);
    
    if (isset($response['success']) && $response['success'] == 1) {
        return true;
    } else {
        error_log("FCM Error: " . json_encode($response));
        return false;
    }
}

?>

