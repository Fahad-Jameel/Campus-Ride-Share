<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$userId = $_GET['id'] ?? null;

if (!$userId) {
    sendResponse(['error' => 'User ID required'], 400);
}

$conn = getDBConnection();

$stmt = $conn->prepare("SELECT id, email, name, phone, profile_image_url, affiliation, verified, created_at FROM users WHERE id = ?");
$stmt->bind_param("s", $userId);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    sendResponse(['error' => 'User not found'], 404);
}

$user = $result->fetch_assoc();

$userData = [
    'id' => $user['id'],
    'email' => $user['email'],
    'name' => $user['name'],
    'phone' => $user['phone'],
    'profileImageUrl' => $user['profile_image_url'],
    'affiliation' => $user['affiliation'],
    'verified' => (bool)$user['verified'],
    'createdAt' => strtotime($user['created_at']) * 1000
];

sendResponse(['success' => true, 'user' => $userData]);

$stmt->close();
$conn->close();

?>

