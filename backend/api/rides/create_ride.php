<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = getRequestBody();
$required = ['driverId', 'pickupLocation', 'destination', 'date', 'time', 'availableSeats', 'totalSeats', 'cost'];
$missing = validateRequired($data, $required);

if (!empty($missing)) {
    sendResponse(['error' => 'Missing required fields: ' . implode(', ', $missing)], 400);
}

$conn = getDBConnection();

// Get driver info
$stmt = $conn->prepare("SELECT name, profile_image_url FROM users WHERE id = ?");
$stmt->bind_param("s", $data['driverId']);
$stmt->execute();
$driverResult = $stmt->get_result();

if ($driverResult->num_rows === 0) {
    sendResponse(['error' => 'Driver not found'], 404);
}

$driver = $driverResult->fetch_assoc();
$rideId = uniqid('ride_', true);

$preferences = isset($data['preferences']) && is_array($data['preferences']) 
    ? json_encode($data['preferences']) 
    : '[]';

$stmt = $conn->prepare("INSERT INTO rides (id, driver_id, driver_name, driver_image_url, pickup_location, destination, date, time, available_seats, total_seats, cost, vehicle_id, vehicle_model, preferences) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

$vehicleId = $data['vehicleId'] ?? null;
$vehicleModel = $data['vehicleModel'] ?? null;

$stmt->bind_param("ssssssssiidsss", 
    $rideId,
    $data['driverId'],
    $driver['name'],
    $driver['profile_image_url'],
    $data['pickupLocation'],
    $data['destination'],
    $data['date'],
    $data['time'],
    $data['availableSeats'],
    $data['totalSeats'],
    $data['cost'],
    $vehicleId,
    $vehicleModel,
    $preferences
);

if ($stmt->execute()) {
    $ride = [
        'id' => $rideId,
        'driverId' => $data['driverId'],
        'driverName' => $driver['name'],
        'driverImageUrl' => $driver['profile_image_url'],
        'pickupLocation' => $data['pickupLocation'],
        'destination' => $data['destination'],
        'date' => $data['date'],
        'time' => $data['time'],
        'availableSeats' => (int)$data['availableSeats'],
        'totalSeats' => (int)$data['totalSeats'],
        'cost' => $data['cost'],
        'vehicleId' => $vehicleId,
        'vehicleModel' => $vehicleModel,
        'preferences' => json_decode($preferences, true),
        'createdAt' => time() * 1000
    ];
    
    sendResponse(['success' => true, 'ride' => $ride], 201);
} else {
    sendResponse(['error' => 'Failed to create ride: ' . $conn->error], 500);
}

$stmt->close();
$conn->close();

?>

