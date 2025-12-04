<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$userId = $_GET['userId'] ?? null;

if (!$userId) {
    sendResponse(['error' => 'User ID required'], 400);
}

$conn = getDBConnection();

$stmt = $conn->prepare("SELECT * FROM vehicles WHERE user_id = ? ORDER BY created_at DESC");
$stmt->bind_param("s", $userId);
$stmt->execute();
$result = $stmt->get_result();

$vehicles = [];
while ($row = $result->fetch_assoc()) {
    $vehicles[] = [
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
}

sendResponse(['success' => true, 'vehicles' => $vehicles]);

$stmt->close();
$conn->close();

?>

