<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once '../config.php';

// Get posted data
$data = json_decode(file_get_contents("php://input"));

// Validate required fields
if (
    !empty($data->ride_id) &&
    !empty($data->passenger_id) &&
    !empty($data->seats_requested) &&
    isset($data->status)
) {
    try {
        $conn = getDBConnection();
        
        // Check if ride exists and has enough seats
        $check_sql = "SELECT available_seats FROM rides WHERE id = ?";
        $check_stmt = $conn->prepare($check_sql);
        $check_stmt->bind_param("s", $data->ride_id);
        $check_stmt->execute();
        $result = $check_stmt->get_result();
        
        if ($result->num_rows == 0) {
            http_response_code(404);
            echo json_encode(array("success" => false, "message" => "Ride not found"));
            $conn->close();
            exit();
        }
        
        $ride = $result->fetch_assoc();
        
        if ($ride['available_seats'] < $data->seats_requested) {
            http_response_code(400);
            echo json_encode(array("success" => false, "message" => "Not enough seats available"));
            $conn->close();
            exit();
        }
        
        // Generate booking ID
        $booking_id = uniqid('booking_', true);
        
        // Insert booking
        $sql = "INSERT INTO bookings 
                (id, ride_id, passenger_id, seats_requested, stop_location, status, created_at) 
                VALUES 
                (?, ?, ?, ?, ?, ?, NOW())";
        
        $stmt = $conn->prepare($sql);
        $stop_location = isset($data->stop_location) ? $data->stop_location : "";
        $stmt->bind_param("sssiss", 
            $booking_id, 
            $data->ride_id, 
            $data->passenger_id, 
            $data->seats_requested,
            $stop_location,
            $data->status
        );
        
        if ($stmt->execute()) {
            http_response_code(201);
            echo json_encode(array(
                "success" => true,
                "message" => "Booking created successfully",
                "booking_id" => $booking_id
            ));
        } else {
            http_response_code(503);
            echo json_encode(array("success" => false, "message" => "Unable to create booking"));
        }
        
        $stmt->close();
        $conn->close();
        
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(array("success" => false, "message" => "Database error: " . $e->getMessage()));
    }
} else {
    http_response_code(400);
    echo json_encode(array("success" => false, "message" => "Incomplete data"));
}
?>
