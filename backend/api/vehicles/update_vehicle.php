<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'PUT') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$vehicleId = $data['id'] ?? null;

if (!$vehicleId) {
    sendResponse(['error' => 'Vehicle ID required'], 400);
}

$conn = getDBConnection();

$fields = [];
$params = [];
$types = '';

if (isset($data['make'])) {
    $fields[] = "make = ?";
    $params[] = $data['make'];
    $types .= 's';
}

if (isset($data['model'])) {
    $fields[] = "model = ?";
    $params[] = $data['model'];
    $types .= 's';
}

if (isset($data['year'])) {
    $fields[] = "year = ?";
    $params[] = $data['year'];
    $types .= 'i';
}

if (isset($data['color'])) {
    $fields[] = "color = ?";
    $params[] = $data['color'];
    $types .= 's';
}

if (isset($data['licensePlate'])) {
    $fields[] = "license_plate = ?";
    $params[] = $data['licensePlate'];
    $types .= 's';
}

if (isset($data['imageUrl'])) {
    $fields[] = "image_url = ?";
    $params[] = $data['imageUrl'];
    $types .= 's';
}

if (empty($fields)) {
    sendResponse(['error' => 'No fields to update'], 400);
}

$params[] = $vehicleId;
$types .= 's';

$sql = "UPDATE vehicles SET " . implode(', ', $fields) . " WHERE id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param($types, ...$params);

if ($stmt->execute()) {
    $stmt = $conn->prepare("SELECT * FROM vehicles WHERE id = ?");
    $stmt->bind_param("s", $vehicleId);
    $stmt->execute();
    $result = $stmt->get_result();
    $row = $result->fetch_assoc();
    
    $vehicle = [
        'id' => $row['id'],
        'userId' => $row['user_id'],
        'make' => $row['make'],
        'model' => $row['model'],
        'year' => (int)$row['year'],
        'color' => $row['color'],
        'licensePlate' => $row['license_plate'],
        'imageUrl' => $row['image_url'],
        'createdAt' => strtotime($row['created_at']) * 1000
    ];
    
    sendResponse(['success' => true, 'vehicle' => $vehicle]);
} else {
    sendResponse(['error' => 'Update failed: ' . $conn->error], 500);
}

$stmt->close();
$conn->close();

?>

