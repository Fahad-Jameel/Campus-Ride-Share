<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once __DIR__ . '/../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

try {
    $conn = getDBConnection();
} catch (Exception $e) {
    sendResponse(['error' => 'Database connection failed: ' . $e->getMessage()], 500);
}

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

try {
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        sendResponse(['error' => 'Prepare failed: ' . $conn->error], 500);
    }
    
    if (!empty($params)) {
        $stmt->bind_param($types, ...$params);
    }
    
    if (!$stmt->execute()) {
        sendResponse(['error' => 'Execute failed: ' . $stmt->error], 500);
    }
    
    $result = $stmt->get_result();
    
    $rides = [];
    while ($row = $result->fetch_assoc()) {
        $preferences = !empty($row['preferences']) ? json_decode($row['preferences'], true) : [];
        if (!is_array($preferences)) {
            $preferences = [];
        }
        
        $rides[] = [
            'id' => $row['id'],
            'driverId' => $row['driver_id'],
            'driverName' => $row['driver_name'] ?? 'Unknown',
            'driverImageUrl' => $row['driver_image_url'] ?? null,
            'pickupLocation' => $row['pickup_location'],
            'destination' => $row['destination'],
            'date' => $row['date'],
            'time' => $row['time'],
            'availableSeats' => (int)$row['available_seats'],
            'totalSeats' => (int)$row['total_seats'],
            'cost' => number_format((float)$row['cost'], 2, '.', ''),
            'vehicleId' => $row['vehicle_id'] ?? null,
            'vehicleModel' => $row['vehicle_model'] ?? null,
            'preferences' => $preferences,
            'createdAt' => isset($row['created_at']) ? strtotime($row['created_at']) * 1000 : time() * 1000
        ];
    }
    
    sendResponse(['success' => true, 'rides' => $rides]);
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    sendResponse(['error' => 'Database error: ' . $e->getMessage()], 500);
}

?>


