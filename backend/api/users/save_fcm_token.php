<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$userId = $data['userId'] ?? null;
$fcmToken = $data['fcmToken'] ?? null;

if (!$userId || !$fcmToken) {
    sendResponse(['error' => 'User ID and FCM token required'], 400);
}

$conn = getDBConnection();

$stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
$stmt->bind_param("ss", $fcmToken, $userId);

if ($stmt->execute()) {
    sendResponse(['success' => true, 'message' => 'FCM token saved successfully']);
} else {
    sendResponse(['error' => 'Failed to save FCM token'], 500);
}

$stmt->close();
$conn->close();

?>

