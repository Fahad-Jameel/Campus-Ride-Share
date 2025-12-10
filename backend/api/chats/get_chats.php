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

// Get chats where user is either user1 or user2
$stmt = $conn->prepare("
    SELECT c.*, 
           CASE 
               WHEN c.user1_id = ? THEN u2.name 
               ELSE u1.name 
           END as other_user_name,
           CASE 
               WHEN c.user1_id = ? THEN u2.profile_image_url 
               ELSE u1.profile_image_url 
           END as other_user_image_url,
           CASE 
               WHEN c.user1_id = ? THEN c.unread_count_user1 
               ELSE c.unread_count_user2 
           END as unread_count
    FROM chats c
    LEFT JOIN users u1 ON c.user1_id = u1.id
    LEFT JOIN users u2 ON c.user2_id = u2.id
    WHERE c.user1_id = ? OR c.user2_id = ?
    ORDER BY c.last_message_time DESC
");

$stmt->bind_param("sssss", $userId, $userId, $userId, $userId, $userId);
$stmt->execute();
$result = $stmt->get_result();

$chats = [];
while ($row = $result->fetch_assoc()) {
    $otherUserId = $row['user1_id'] === $userId ? $row['user2_id'] : $row['user1_id'];
    
    $chats[] = [
        'id' => $row['id'],
        'userId' => $userId,
        'otherUserId' => $otherUserId,
        'otherUserName' => $row['other_user_name'],
        'otherUserImageUrl' => $row['other_user_image_url'],
        'lastMessage' => $row['last_message'],
        'lastMessageTime' => $row['last_message_time'] ? strtotime($row['last_message_time']) * 1000 : null,
        'unreadCount' => (int)$row['unread_count'],
        'createdAt' => strtotime($row['created_at']) * 1000
    ];
}

sendResponse(['success' => true, 'chats' => $chats]);

$stmt->close();
$conn->close();

?>


