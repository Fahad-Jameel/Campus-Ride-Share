<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

include_once '../config.php';

// Get query parameters
$passenger_id = isset($_GET['passenger_id']) ? $_GET['passenger_id'] : null;
$driver_id = isset($_GET['driver_id']) ? $_GET['driver_id'] : null;

try {
    $conn = getDBConnection();
    
    if ($passenger_id) {
        // Get bookings for passenger with ride details
        $query = "SELECT 
                    b.*,
                    r.pickup_location,
                    r.destination,
                    r.date,
                    r.time,
                    r.cost,
                    r.driver_id,
                    r.driver_name,
                    r.vehicle_model
                  FROM bookings b
                  LEFT JOIN rides r ON b.ride_id = r.id
                  WHERE b.passenger_id = ?
                  ORDER BY b.created_at DESC";
        
        $stmt = $conn->prepare($query);
        $stmt->bind_param("s", $passenger_id);
        
    } elseif ($driver_id) {
        // Get bookings for driver's rides
        $query = "SELECT 
                    b.*,
                    r.pickup_location,
                    r.destination,
                    r.date,
                    r.time,
                    r.cost,
                    u.name as passenger_name,
                    u.email as passenger_email,
                    u.phone as passenger_phone
                  FROM bookings b
                  LEFT JOIN rides r ON b.ride_id = r.id
                  LEFT JOIN users u ON b.passenger_id = u.id
                  WHERE r.driver_id = ?
                  ORDER BY b.created_at DESC";
        
        $stmt = $conn->prepare($query);
        $stmt->bind_param("s", $driver_id);
        
    } else {
        http_response_code(400);
        echo json_encode(array("success" => false, "message" => "passenger_id or driver_id required"));
        $conn->close();
        exit();
    }
    
    $stmt->execute();
    $result = $stmt->get_result();
    
    $bookings = [];
    while ($row = $result->fetch_assoc()) {
        $bookings[] = $row;
    }
    
    http_response_code(200);
    echo json_encode($bookings);
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array("success" => false, "message" => "Database error: " . $e->getMessage()));
}
?>
