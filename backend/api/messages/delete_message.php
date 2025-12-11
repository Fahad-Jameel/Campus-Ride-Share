<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = json_decode(file_get_contents("php://input"));

if (empty($data->message_id)) {
    sendResponse(['error' => 'Message ID required'], 400);
}

$conn = getDBConnection();

// Delete message (soft delete - set text to null or mark as deleted)
// For now, we'll actually delete it
$sql = "DELETE FROM messages WHERE id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $data->message_id);

if ($stmt->execute()) {
    sendResponse(['success' => true, 'message' => 'Message deleted']);
} else {
    sendResponse(['error' => 'Failed to delete message'], 500);
}

$stmt->close();
$conn->close();

?>

