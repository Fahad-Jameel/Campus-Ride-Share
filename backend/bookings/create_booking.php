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
            // Get ride and driver information
            $ride_sql = "SELECT driver_id, pickup_location, destination FROM rides WHERE id = ?";
            $ride_stmt = $conn->prepare($ride_sql);
            $ride_stmt->bind_param("s", $data->ride_id);
            $ride_stmt->execute();
            $ride_result = $ride_stmt->get_result();
            
            if ($ride_result->num_rows > 0) {
                $ride_data = $ride_result->fetch_assoc();
                $driver_id = $ride_data['driver_id'];
                
                // Get passenger name
                $passenger_sql = "SELECT name FROM users WHERE id = ?";
                $passenger_stmt = $conn->prepare($passenger_sql);
                $passenger_stmt->bind_param("s", $data->passenger_id);
                $passenger_stmt->execute();
                $passenger_result = $passenger_stmt->get_result();
                $passenger_name = "A passenger";
                if ($passenger_result->num_rows > 0) {
                    $passenger_data = $passenger_result->fetch_assoc();
                    $passenger_name = $passenger_data['name'];
                }
                $passenger_stmt->close();
                
                // Get driver's FCM token
                $fcm_sql = "SELECT fcm_token FROM users WHERE id = ?";
                $fcm_stmt = $conn->prepare($fcm_sql);
                $fcm_stmt->bind_param("s", $driver_id);
                $fcm_stmt->execute();
                $fcm_result = $fcm_stmt->get_result();
                
                if ($fcm_result->num_rows > 0) {
                    $fcm_data = $fcm_result->fetch_assoc();
                    $fcm_token = $fcm_data['fcm_token'];
                    
                    if (!empty($fcm_token)) {
                        // Send push notification
                        require_once __DIR__ . '/../api/notifications/send_fcm_notification.php';
                        
                        $title = "New Booking Request";
                        $body = "$passenger_name requested {$data->seats_requested} seat(s) for your ride from {$ride_data['pickup_location']} to {$ride_data['destination']}";
                        
                        $notification_data = [
                            'type' => 'booking_request',
                            'booking_id' => $booking_id,
                            'ride_id' => $data->ride_id,
                            'passenger_id' => $data->passenger_id,
                            'seats_requested' => (string)$data->seats_requested
                        ];
                        
                        sendFCMNotification($fcm_token, $title, $body, $notification_data);
                    }
                }
                $fcm_stmt->close();
                
                // Create notification record in database
                $notification_id = uniqid('notif_', true);
                $notif_sql = "INSERT INTO notifications (id, user_id, type, title, message, booking_id, ride_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                $notif_stmt = $conn->prepare($notif_sql);
                $notif_type = 'booking_request';
                $notif_title = "New Booking Request";
                $notif_message = "$passenger_name requested {$data->seats_requested} seat(s) for your ride";
                $notif_stmt->bind_param("sssssss", 
                    $notification_id,
                    $driver_id,
                    $notif_type,
                    $notif_title,
                    $notif_message,
                    $booking_id,
                    $data->ride_id
                );
                $notif_stmt->execute();
                $notif_stmt->close();
            }
            $ride_stmt->close();
            
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
