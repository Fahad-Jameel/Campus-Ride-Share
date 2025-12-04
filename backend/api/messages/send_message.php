<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$required = ['chatId', 'senderId', 'receiverId', 'text'];
$missing = validateRequired($data, $required);

if (!empty($missing)) {
    sendResponse(['error' => 'Missing required fields: ' . implode(', ', $missing)], 400);
}

$conn = getDBConnection();

$messageId = uniqid('msg_', true);
$imageUrl = $data['imageUrl'] ?? null;

$stmt = $conn->prepare("INSERT INTO messages (id, chat_id, sender_id, receiver_id, text, image_url) VALUES (?, ?, ?, ?, ?, ?)");
$stmt->bind_param("ssssss", $messageId, $data['chatId'], $data['senderId'], $data['receiverId'], $data['text'], $imageUrl);

if ($stmt->execute()) {
    // Update chat's last message
    $updateChat = $conn->prepare("UPDATE chats SET last_message = ?, last_message_time = NOW() WHERE id = ?");
    $updateChat->bind_param("ss", $data['text'], $data['chatId']);
    $updateChat->execute();
    
    // Update unread count
    $updateUnread = $conn->prepare("UPDATE chats SET unread_count_user2 = unread_count_user2 + 1 WHERE id = ? AND user2_id = ?");
    $updateUnread->bind_param("ss", $data['chatId'], $data['receiverId']);
    $updateUnread->execute();
    
    $message = [
        'id' => $messageId,
        'chatId' => $data['chatId'],
        'senderId' => $data['senderId'],
        'receiverId' => $data['receiverId'],
        'text' => $data['text'],
        'imageUrl' => $imageUrl,
        'isRead' => false,
        'timestamp' => time() * 1000
    ];
    
    sendResponse(['success' => true, 'message' => $message], 201);
} else {
    sendResponse(['error' => 'Failed to send message: ' . $conn->error], 500);
}

$stmt->close();
$conn->close();

?>

