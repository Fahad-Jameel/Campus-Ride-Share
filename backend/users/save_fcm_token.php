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
if (!empty($data->user_id) && !empty($data->fcm_token)) {
    try {
        $conn = getDBConnection();
        
        // Update or insert FCM token
        $sql = "UPDATE users SET fcm_token = ? WHERE id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ss", $data->fcm_token, $data->user_id);
        
        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(array(
                "success" => true,
                "message" => "FCM token saved successfully"
            ));
        } else {
            http_response_code(503);
            echo json_encode(array("success" => false, "message" => "Unable to save token"));
        }
        
        $stmt->close();
        $conn->close();
        
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(array("success" => false, "message" => "Database error: " . $e->getMessage()));
    }
} else {
    http_response_code(400);
    echo json_encode(array("success" => false, "message" => "user_id and fcm_token required"));
}
?>
