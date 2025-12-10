<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

if (!isset($_FILES['image'])) {
    sendResponse(['error' => 'No image file provided'], 400);
}

$file = $_FILES['image'];
$type = $_POST['type'] ?? 'general'; // 'profile', 'vehicle', 'ride', 'general'

// Validate file
$allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
if (!in_array($file['type'], $allowedTypes)) {
    sendResponse(['error' => 'Invalid file type. Only JPEG, PNG, and GIF are allowed.'], 400);
}

if ($file['size'] > 5 * 1024 * 1024) { // 5MB limit
    sendResponse(['error' => 'File size exceeds 5MB limit'], 400);
}

// Create uploads directory if it doesn't exist
$uploadDir = '../uploads/' . $type . '/';
if (!file_exists($uploadDir)) {
    mkdir($uploadDir, 0777, true);
}

// Generate unique filename
$extension = pathinfo($file['name'], PATHINFO_EXTENSION);
$filename = uniqid('img_', true) . '.' . $extension;
$filepath = $uploadDir . $filename;

// Move uploaded file
if (move_uploaded_file($file['tmp_name'], $filepath)) {
    // Return URL (adjust base URL as needed)
    $baseUrl = 'http://your-domain.com/api/uploads/' . $type . '/';
    $imageUrl = $baseUrl . $filename;
    
    sendResponse([
        'success' => true,
        'imageUrl' => $imageUrl,
        'filename' => $filename
    ], 201);
} else {
    sendResponse(['error' => 'Failed to upload image'], 500);
}

?>


