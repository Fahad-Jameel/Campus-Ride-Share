<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$required = ['email', 'password'];
$missing = validateRequired($data, $required);

if (!empty($missing)) {
    sendResponse(['error' => 'Missing required fields: ' . implode(', ', $missing)], 400);
}

$conn = getDBConnection();

$stmt = $conn->prepare("SELECT id, email, password_hash, name, phone, profile_image_url, affiliation, verified, created_at FROM users WHERE email = ?");
$stmt->bind_param("s", $data['email']);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    sendResponse(['error' => 'Invalid email or password'], 401);
}

$user = $result->fetch_assoc();

if (!password_verify($data['password'], $user['password_hash'])) {
    sendResponse(['error' => 'Invalid email or password'], 401);
}

// Return user data (without password)
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

sendResponse([
    'success' => true,
    'user' => $userData,
    'token' => generateToken($user['id'])
]);

$stmt->close();
$conn->close();

function generateToken($userId) {
    return base64_encode($userId . ':' . time());
}

?>

