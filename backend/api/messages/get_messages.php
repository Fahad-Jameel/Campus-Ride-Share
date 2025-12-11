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

$chatId = $_GET['chatId'] ?? null;

if (!$chatId) {
    sendResponse(['error' => 'Chat ID required'], 400);
}

try {
    $conn = getDBConnection();
} catch (Exception $e) {
    sendResponse(['error' => 'Database connection failed: ' . $e->getMessage()], 500);
}

try {
    $stmt = $conn->prepare("SELECT * FROM messages WHERE chat_id = ? ORDER BY timestamp ASC");
    if (!$stmt) {
        sendResponse(['error' => 'Prepare failed: ' . $conn->error], 500);
    }
    
    $stmt->bind_param("s", $chatId);
    
    if (!$stmt->execute()) {
        sendResponse(['error' => 'Execute failed: ' . $stmt->error], 500);
    }
    
    $result = $stmt->get_result();
    
    $messages = [];
    while ($row = $result->fetch_assoc()) {
        $messages[] = [
            'id' => $row['id'],
            'chatId' => $row['chat_id'],
            'senderId' => $row['sender_id'],
            'receiverId' => $row['receiver_id'],
            'text' => $row['text'] ?? '',
            'imageUrl' => $row['image_url'] ?? null,
            'isRead' => (bool)($row['is_read'] ?? false),
            'timestamp' => isset($row['timestamp']) ? strtotime($row['timestamp']) * 1000 : time() * 1000
        ];
    }
    
    sendResponse(['success' => true, 'messages' => $messages]);
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
}

?>


