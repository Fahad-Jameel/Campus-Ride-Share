<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$conn = getDBConnection();

$pickup = $_GET['pickup'] ?? null;
$destination = $_GET['destination'] ?? null;
$date = $_GET['date'] ?? null;
$search = $_GET['search'] ?? null;

$sql = "SELECT r.*, u.name as driver_name, u.profile_image_url as driver_image_url 
        FROM rides r 
        JOIN users u ON r.driver_id = u.id 
        WHERE 1=1";
$params = [];
$types = '';

if ($pickup) {
    $sql .= " AND r.pickup_location LIKE ?";
    $params[] = "%$pickup%";
    $types .= 's';
}

if ($destination) {
    $sql .= " AND r.destination LIKE ?";
    $params[] = "%$destination%";
    $types .= 's';
}

if ($date) {
    $sql .= " AND r.date = ?";
    $params[] = $date;
    $types .= 's';
}

if ($search) {
    $sql .= " AND (r.pickup_location LIKE ? OR r.destination LIKE ?)";
    $params[] = "%$search%";
    $params[] = "%$search%";
    $types .= 'ss';
}

$sql .= " ORDER BY r.created_at DESC LIMIT 50";

$stmt = $conn->prepare($sql);
if (!empty($params)) {
    $stmt->bind_param($types, ...$params);
}
$stmt->execute();
$result = $stmt->get_result();

$rides = [];
while ($row = $result->fetch_assoc()) {
    $preferences = !empty($row['preferences']) ? json_decode($row['preferences'], true) : [];
    
    $rides[] = [
        'id' => $row['id'],
        'driverId' => $row['driver_id'],
        'driverName' => $row['driver_name'],
        'driverImageUrl' => $row['driver_image_url'],
        'pickupLocation' => $row['pickup_location'],
        'destination' => $row['destination'],
        'date' => $row['date'],
        'time' => $row['time'],
        'availableSeats' => (int)$row['available_seats'],
        'totalSeats' => (int)$row['total_seats'],
        'cost' => number_format((float)$row['cost'], 2, '.', ''),
        'vehicleId' => $row['vehicle_id'],
        'vehicleModel' => $row['vehicle_model'],
        'preferences' => $preferences,
        'createdAt' => strtotime($row['created_at']) * 1000
    ];
}

sendResponse(['success' => true, 'rides' => $rides]);

$stmt->close();
$conn->close();

?>


