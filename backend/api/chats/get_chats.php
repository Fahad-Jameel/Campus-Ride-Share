<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once __DIR__ . '/../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$userId = $_GET['userId'] ?? null;

if (!$userId) {
    sendResponse(['error' => 'User ID required'], 400);
}

try {
    $conn = getDBConnection();
} catch (Exception $e) {
    sendResponse(['error' => 'Database connection failed: ' . $e->getMessage()], 500);
}

try {
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
    
    if (!$stmt) {
        sendResponse(['error' => 'Prepare failed: ' . $conn->error], 500);
    }
    
    $stmt->bind_param("sssss", $userId, $userId, $userId, $userId, $userId);
    
    if (!$stmt->execute()) {
        sendResponse(['error' => 'Execute failed: ' . $stmt->error], 500);
    }
    
    $result = $stmt->get_result();
    
    $chats = [];
    while ($row = $result->fetch_assoc()) {
        $otherUserId = $row['user1_id'] === $userId ? $row['user2_id'] : $row['user1_id'];
        
        $chats[] = [
            'id' => $row['id'],
            'userId' => $userId,
            'otherUserId' => $otherUserId,
            'otherUserName' => $row['other_user_name'] ?? 'Unknown',
            'otherUserImageUrl' => $row['other_user_image_url'] ?? null,
            'lastMessage' => $row['last_message'] ?? null,
            'lastMessageTime' => isset($row['last_message_time']) && $row['last_message_time'] ? strtotime($row['last_message_time']) * 1000 : null,
            'unreadCount' => (int)($row['unread_count'] ?? 0),
            'createdAt' => isset($row['created_at']) ? strtotime($row['created_at']) * 1000 : time() * 1000
        ];
    }
    
    sendResponse(['success' => true, 'chats' => $chats]);
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
}

?>


