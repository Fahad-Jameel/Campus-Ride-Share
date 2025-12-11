<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = json_decode(file_get_contents("php://input"));

if (empty($data->message_id) || empty($data->text)) {
    sendResponse(['error' => 'Message ID and text required'], 400);
}

$conn = getDBConnection();

// Update message text
$sql = "UPDATE messages SET text = ?, updated_at = NOW() WHERE id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $data->text, $data->message_id);

if ($stmt->execute()) {
    // Get updated message
    $get_sql = "SELECT * FROM messages WHERE id = ?";
    $get_stmt = $conn->prepare($get_sql);
    $get_stmt->bind_param("s", $data->message_id);
    $get_stmt->execute();
    $result = $get_stmt->get_result();
    $message = $result->fetch_assoc();
    
    sendResponse([
        'success' => true,
        'message' => [
            'id' => $message['id'],
            'chatId' => $message['chat_id'],
            'senderId' => $message['sender_id'],
            'receiverId' => $message['receiver_id'],
            'text' => $message['text'],
            'imageUrl' => $message['image_url'],
            'isRead' => (bool)$message['is_read'],
            'timestamp' => strtotime($message['timestamp']) * 1000,
            'isEdited' => true
        ]
    ]);
} else {
    sendResponse(['error' => 'Failed to update message'], 500);
}

$stmt->close();
$conn->close();

?>

