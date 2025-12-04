<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$required = ['userId', 'make', 'model', 'year', 'color', 'licensePlate'];
$missing = validateRequired($data, $required);

if (!empty($missing)) {
    sendResponse(['error' => 'Missing required fields: ' . implode(', ', $missing)], 400);
}

$conn = getDBConnection();

$vehicleId = uniqid('vehicle_', true);

$stmt = $conn->prepare("INSERT INTO vehicles (id, user_id, make, model, year, color, license_plate, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

$imageUrl = $data['imageUrl'] ?? null;

$stmt->bind_param("ssssisss", 
    $vehicleId,
    $data['userId'],
    $data['make'],
    $data['model'],
    $data['year'],
    $data['color'],
    $data['licensePlate'],
    $imageUrl
);

if ($stmt->execute()) {
    $vehicle = [
        'id' => $vehicleId,
        'userId' => $data['userId'],
        'make' => $data['make'],
        'model' => $data['model'],
        'year' => (int)$data['year'],
        'color' => $data['color'],
        'licensePlate' => $data['licensePlate'],
        'imageUrl' => $imageUrl,
        'createdAt' => time() * 1000
    ];
    
    sendResponse(['success' => true, 'vehicle' => $vehicle], 201);
} else {
    sendResponse(['error' => 'Failed to create vehicle: ' . $conn->error], 500);
}

$stmt->close();
$conn->close();

?>

