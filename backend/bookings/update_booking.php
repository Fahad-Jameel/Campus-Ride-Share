<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once '../config.php';

// Get booking ID from query parameter
$booking_id = isset($_GET['id']) ? $_GET['id'] : null;

// Get posted data
$data = json_decode(file_get_contents("php://input"));

if (!$booking_id || !isset($data->status)) {
    http_response_code(400);
    echo json_encode(array("success" => false, "message" => "Booking ID and status required"));
    exit();
}

try {
    $conn = getDBConnection();
    
    // Get current booking info
    $check_sql = "SELECT b.*, r.available_seats, r.id as ride_id, r.driver_id,
                         u.fcm_token as passenger_token, u.name as passenger_name
                  FROM bookings b 
                  LEFT JOIN rides r ON b.ride_id = r.id 
                  LEFT JOIN users u ON b.passenger_id = u.id
                  WHERE b.id = ?";
    $check_stmt = $conn->prepare($check_sql);
    $check_stmt->bind_param("s", $booking_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();
    
    if ($result->num_rows == 0) {
        http_response_code(404);
        echo json_encode(array("success" => false, "message" => "Booking not found"));
        $conn->close();
        exit();
    }
    
    $booking = $result->fetch_assoc();
    $old_status = $booking['status'];
    $new_status = $data->status;
    $rejection_reason = isset($data->rejection_reason) ? $data->rejection_reason : null;
    
    // Update booking status
    if ($rejection_reason) {
        $sql = "UPDATE bookings SET status = ?, rejection_reason = ? WHERE id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("sss", $new_status, $rejection_reason, $booking_id);
    } else {
        $sql = "UPDATE bookings SET status = ? WHERE id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ss", $new_status, $booking_id);
    }
    
    if ($stmt->execute()) {
        // If status changed to 'accepted', decrease available seats
        if ($old_status == 'pending' && $new_status == 'accepted') {
            $update_seats = "UPDATE rides 
                           SET available_seats = available_seats - ? 
                           WHERE id = ? AND available_seats >= ?";
            $seats_stmt = $conn->prepare($update_seats);
            $seats = $booking['seats_requested'];
            $ride_id = $booking['ride_id'];
            $seats_stmt->bind_param("isi", $seats, $ride_id, $seats);
            $seats_stmt->execute();
            $seats_stmt->close();
        }
        // If status changed from 'accepted' to 'cancelled', increase available seats
        elseif ($old_status == 'accepted' && $new_status == 'cancelled') {
            $update_seats = "UPDATE rides 
                           SET available_seats = available_seats + ? 
                           WHERE id = ?";
            $seats_stmt = $conn->prepare($update_seats);
            $seats = $booking['seats_requested'];
            $ride_id = $booking['ride_id'];
            $seats_stmt->bind_param("is", $seats, $ride_id);
            $seats_stmt->execute();
            $seats_stmt->close();
        }
        
        // TODO: Send FCM notification to passenger
        // For now, just log it would be sent
        
        http_response_code(200);
        echo json_encode(array(
            "success" => true,
            "message" => "Booking status updated successfully"
        ));
    } else {
        http_response_code(503);
        echo json_encode(array("success" => false, "message" => "Unable to update booking"));
    }
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array("success" => false, "message" => "Database error: " . $e->getMessage()));
}
?>
