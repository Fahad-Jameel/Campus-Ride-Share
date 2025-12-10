<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$userId = $_GET['userId'] ?? null;

if (!$userId) {
    sendResponse(['error' => 'User ID required'], 400);
}

$conn = getDBConnection();

// Get unread count
$unread_sql = "SELECT COUNT(*) as unread_count FROM notifications WHERE user_id = ? AND is_read = FALSE";
$unread_stmt = $conn->prepare($unread_sql);
$unread_stmt->bind_param("s", $userId);
$unread_stmt->execute();
$unread_result = $unread_stmt->get_result();
$unread_count = 0;
if ($unread_result->num_rows > 0) {
    $unread_data = $unread_result->fetch_assoc();
    $unread_count = (int)$unread_data['unread_count'];
}
$unread_stmt->close();

// Get notifications (most recent first)
$sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $userId);
$stmt->execute();
$result = $stmt->get_result();

$notifications = [];
while ($row = $result->fetch_assoc()) {
    $notifications[] = [
        'id' => $row['id'],
        'userId' => $row['user_id'],
        'type' => $row['type'],
        'title' => $row['title'],
        'message' => $row['message'],
        'bookingId' => $row['booking_id'],
        'rideId' => $row['ride_id'],
        'isRead' => (bool)$row['is_read'],
        'createdAt' => strtotime($row['created_at']) * 1000
    ];
}

sendResponse([
    'success' => true,
    'notifications' => $notifications,
    'unreadCount' => $unread_count
]);

$stmt->close();
$conn->close();

?>

