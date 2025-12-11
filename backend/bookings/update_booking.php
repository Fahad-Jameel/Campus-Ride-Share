<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

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
    
    if (!$conn) {
        throw new Exception("Database connection failed");
    }
    
    // Get current booking info
    $check_sql = "SELECT b.*, r.available_seats, r.id as ride_id, r.driver_id,
                         r.pickup_location, r.destination,
                         u.fcm_token as passenger_token, u.name as passenger_name, u.id as passenger_id,
                         d.name as driver_name, d.id as driver_id_check
                  FROM bookings b 
                  LEFT JOIN rides r ON b.ride_id = r.id 
                  LEFT JOIN users u ON b.passenger_id = u.id
                  LEFT JOIN users d ON r.driver_id = d.id
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
        
        // Send notification to passenger when status changes
        $passenger_id = $booking['passenger_id'];
        $passenger_token = $booking['passenger_token'];
        $driver_name = $booking['driver_name'] ?? "Driver";
        $pickup = $booking['pickup_location'] ?? "";
        $destination = $booking['destination'] ?? "";
        
        if ($new_status == 'accepted') {
            // Send acceptance notification to passenger
            if (!empty($passenger_token)) {
                require_once __DIR__ . '/../notifications/send_fcm_notification.php';
                
                $title = "Booking Accepted!";
                $body = "$driver_name accepted your booking request for {$booking['seats_requested']} seat(s) from $pickup to $destination";
                
                $notification_data = [
                    'type' => 'booking_accepted',
                    'booking_id' => $booking_id,
                    'ride_id' => $booking['ride_id'],
                    'driver_id' => $booking['driver_id']
                ];
                
                sendFCMNotification($passenger_token, $title, $body, $notification_data);
            }
            
            // Create notification record in database for passenger
            $notification_id = uniqid('notif_', true);
            $notif_sql = "INSERT INTO notifications (id, user_id, type, title, message, booking_id, ride_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            $notif_stmt = $conn->prepare($notif_sql);
            $notif_type = 'booking_accepted';
            $notif_title = "Booking Accepted!";
            $notif_message = "$driver_name accepted your booking request for {$booking['seats_requested']} seat(s)";
            $notif_stmt->bind_param("sssssss", 
                $notification_id,
                $passenger_id,
                $notif_type,
                $notif_title,
                $notif_message,
                $booking_id,
                $booking['ride_id']
            );
            $notif_stmt->execute();
            $notif_stmt->close();
            
            // Create chat between driver and passenger
            $driver_id = $booking['driver_id'];
            $chat_sql = "SELECT id FROM chats 
                         WHERE (user1_id = ? AND user2_id = ?) 
                         OR (user1_id = ? AND user2_id = ?)";
            $chat_check_stmt = $conn->prepare($chat_sql);
            $chat_check_stmt->bind_param("ssss", $driver_id, $passenger_id, $passenger_id, $driver_id);
            $chat_check_stmt->execute();
            $chat_check_result = $chat_check_stmt->get_result();
            
            if ($chat_check_result->num_rows == 0) {
                // Create new chat
                $chat_id = uniqid('chat_', true);
                $chat_insert_sql = "INSERT INTO chats (id, user1_id, user2_id, created_at) VALUES (?, ?, ?, NOW())";
                $chat_insert_stmt = $conn->prepare($chat_insert_sql);
                $chat_insert_stmt->bind_param("sss", $chat_id, $driver_id, $passenger_id);
                $chat_insert_stmt->execute();
                $chat_insert_stmt->close();
            }
            $chat_check_stmt->close();
            
        } elseif ($new_status == 'rejected') {
            // Send rejection notification to passenger
            if (!empty($passenger_token)) {
                require_once __DIR__ . '/../notifications/send_fcm_notification.php';
                
                $title = "Booking Request Rejected";
                $reason_text = $rejection_reason ? "\nReason: $rejection_reason" : "";
                $body = "$driver_name rejected your booking request for {$booking['seats_requested']} seat(s)$reason_text";
                
                $notification_data = [
                    'type' => 'booking_rejected',
                    'booking_id' => $booking_id,
                    'ride_id' => $booking['ride_id'],
                    'driver_id' => $booking['driver_id'],
                    'reason' => $rejection_reason ?? ''
                ];
                
                sendFCMNotification($passenger_token, $title, $body, $notification_data);
            }
            
            // Create notification record in database for passenger
            $notification_id = uniqid('notif_', true);
            $notif_sql = "INSERT INTO notifications (id, user_id, type, title, message, booking_id, ride_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            $notif_stmt = $conn->prepare($notif_sql);
            $notif_type = 'booking_rejected';
            $notif_title = "Booking Request Rejected";
            $notif_message = "$driver_name rejected your booking request for {$booking['seats_requested']} seat(s)";
            if ($rejection_reason) {
                $notif_message .= ". Reason: $rejection_reason";
            }
            $notif_stmt->bind_param("sssssss", 
                $notification_id,
                $passenger_id,
                $notif_type,
                $notif_title,
                $notif_message,
                $booking_id,
                $booking['ride_id']
            );
            $notif_stmt->execute();
            $notif_stmt->close();
        }
        
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
