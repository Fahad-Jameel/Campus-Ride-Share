<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$required = ['email', 'password', 'name'];
$missing = validateRequired($data, $required);

if (!empty($missing)) {
    sendResponse(['error' => 'Missing required fields: ' . implode(', ', $missing)], 400);
}

$conn = getDBConnection();

// Check if user already exists
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
$stmt->bind_param("s", $data['email']);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    sendResponse(['error' => 'User with this email already exists'], 409);
}

// Generate unique ID
$userId = uniqid('user_', true);

// Hash password
$passwordHash = password_hash($data['password'], PASSWORD_DEFAULT);

// Insert user
$stmt = $conn->prepare("INSERT INTO users (id, email, password_hash, name, phone, affiliation) VALUES (?, ?, ?, ?, ?, ?)");
$phone = $data['phone'] ?? null;
$affiliation = $data['affiliation'] ?? null;
$stmt->bind_param("ssssss", $userId, $data['email'], $passwordHash, $data['name'], $phone, $affiliation);

if ($stmt->execute()) {
    // Return user data (without password)
    $user = [
        'id' => $userId,
        'email' => $data['email'],
        'name' => $data['name'],
        'phone' => $phone,
        'affiliation' => $affiliation,
        'verified' => false,
        'createdAt' => time() * 1000
    ];
    sendResponse(['success' => true, 'user' => $user, 'token' => generateToken($userId)], 201);
} else {
    sendResponse(['error' => 'Registration failed: ' . $conn->error], 500);
}

$stmt->close();
$conn->close();

function generateToken($userId) {
    // Simple token generation (in production, use JWT)
    return base64_encode($userId . ':' . time());
}

?>

