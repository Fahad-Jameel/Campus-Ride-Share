<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'PUT') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$userId = $data['id'] ?? null;

if (!$userId) {
    sendResponse(['error' => 'User ID required'], 400);
}

$conn = getDBConnection();

$fields = [];
$params = [];
$types = '';

if (isset($data['name'])) {
    $fields[] = "name = ?";
    $params[] = $data['name'];
    $types .= 's';
}

if (isset($data['phone'])) {
    $fields[] = "phone = ?";
    $params[] = $data['phone'];
    $types .= 's';
}

if (isset($data['affiliation'])) {
    $fields[] = "affiliation = ?";
    $params[] = $data['affiliation'];
    $types .= 's';
}

if (isset($data['profileImageUrl'])) {
    $fields[] = "profile_image_url = ?";
    $params[] = $data['profileImageUrl'];
    $types .= 's';
}

if (empty($fields)) {
    sendResponse(['error' => 'No fields to update'], 400);
}

$params[] = $userId;
$types .= 's';

$sql = "UPDATE users SET " . implode(', ', $fields) . " WHERE id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param($types, ...$params);

if ($stmt->execute()) {
    // Fetch updated user
    $stmt = $conn->prepare("SELECT id, email, name, phone, profile_image_url, affiliation, verified, created_at FROM users WHERE id = ?");
    $stmt->bind_param("s", $userId);
    $stmt->execute();
    $result = $stmt->get_result();
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
} else {
    sendResponse(['error' => 'Update failed: ' . $conn->error], 500);
}

$stmt->close();
$conn->close();

?>


