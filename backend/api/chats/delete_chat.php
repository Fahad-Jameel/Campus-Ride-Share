<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = json_decode(file_get_contents("php://input"));

if (empty($data->chat_id)) {
    sendResponse(['error' => 'Chat ID required'], 400);
}

$conn = getDBConnection();

// Delete chat (this will cascade delete all messages due to foreign key)
$sql = "DELETE FROM chats WHERE id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $data->chat_id);

if ($stmt->execute()) {
    sendResponse(['success' => true, 'message' => 'Chat deleted']);
} else {
    sendResponse(['error' => 'Failed to delete chat'], 500);
}

$stmt->close();
$conn->close();

?>

