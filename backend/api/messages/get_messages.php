<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$chatId = $_GET['chatId'] ?? null;

if (!$chatId) {
    sendResponse(['error' => 'Chat ID required'], 400);
}

$conn = getDBConnection();

$stmt = $conn->prepare("SELECT * FROM messages WHERE chat_id = ? ORDER BY timestamp ASC");
$stmt->bind_param("s", $chatId);
$stmt->execute();
$result = $stmt->get_result();

$messages = [];
while ($row = $result->fetch_assoc()) {
    $messages[] = [
        'id' => $row['id'],
        'chatId' => $row['chat_id'],
        'senderId' => $row['sender_id'],
        'receiverId' => $row['receiver_id'],
        'text' => $row['text'],
        'imageUrl' => $row['image_url'],
        'isRead' => (bool)$row['is_read'],
        'timestamp' => strtotime($row['timestamp']) * 1000
    ];
}

sendResponse(['success' => true, 'messages' => $messages]);

$stmt->close();
$conn->close();

?>

