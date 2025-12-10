<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$notificationId = $data['notificationId'] ?? null;
$userId = $data['userId'] ?? null;

if (!$notificationId || !$userId) {
    sendResponse(['error' => 'Notification ID and User ID required'], 400);
}

$conn = getDBConnection();

// Mark notification as read
$sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $notificationId, $userId);

if ($stmt->execute()) {
    sendResponse(['success' => true, 'message' => 'Notification marked as read']);
} else {
    sendResponse(['error' => 'Failed to update notification'], 500);
}

$stmt->close();
$conn->close();

?>

