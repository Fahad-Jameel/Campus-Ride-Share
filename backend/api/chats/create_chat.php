<?php
require_once '../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(['error' => 'Method not allowed'], 405);
}

$data = json_decode(file_get_contents("php://input"));

if (empty($data->user1_id) || empty($data->user2_id)) {
    sendResponse(['error' => 'Both user IDs required'], 400);
}

$conn = getDBConnection();

// Check if chat already exists
$check_sql = "SELECT id FROM chats 
              WHERE (user1_id = ? AND user2_id = ?) 
              OR (user1_id = ? AND user2_id = ?)";
$check_stmt = $conn->prepare($check_sql);
$check_stmt->bind_param("ssss", $data->user1_id, $data->user2_id, $data->user2_id, $data->user1_id);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows > 0) {
    // Chat already exists, return it
    $existing_chat = $check_result->fetch_assoc();
    $chat_id = $existing_chat['id'];
    
    // Get chat details
    $get_sql = "SELECT c.*, 
                CASE 
                    WHEN c.user1_id = ? THEN u2.name 
                    ELSE u1.name 
                END as other_user_name,
                CASE 
                    WHEN c.user1_id = ? THEN u2.profile_image_url 
                    ELSE u1.profile_image_url 
                END as other_user_image_url
                FROM chats c
                LEFT JOIN users u1 ON c.user1_id = u1.id
                LEFT JOIN users u2 ON c.user2_id = u2.id
                WHERE c.id = ?";
    $get_stmt = $conn->prepare($get_sql);
    $get_stmt->bind_param("sss", $data->user1_id, $data->user1_id, $chat_id);
    $get_stmt->execute();
    $chat_result = $get_stmt->get_result();
    $chat_data = $chat_result->fetch_assoc();
    
    $otherUserId = $chat_data['user1_id'] === $data->user1_id ? $chat_data['user2_id'] : $chat_data['user1_id'];
    
    sendResponse([
        'success' => true,
        'chat' => [
            'id' => $chat_id,
            'userId' => $data->user1_id,
            'otherUserId' => $otherUserId,
            'otherUserName' => $chat_data['other_user_name'],
            'otherUserImageUrl' => $chat_data['other_user_image_url'],
            'lastMessage' => $chat_data['last_message'],
            'lastMessageTime' => $chat_data['last_message_time'] ? strtotime($chat_data['last_message_time']) * 1000 : null,
            'unreadCount' => 0,
            'createdAt' => strtotime($chat_data['created_at']) * 1000
        ]
    ]);
    
    $get_stmt->close();
    $check_stmt->close();
    $conn->close();
    exit();
}

// Create new chat
$chat_id = uniqid('chat_', true);

$sql = "INSERT INTO chats (id, user1_id, user2_id, created_at) VALUES (?, ?, ?, NOW())";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $chat_id, $data->user1_id, $data->user2_id);

if ($stmt->execute()) {
    // Get chat with user details
    $get_sql = "SELECT c.*, 
                u1.name as user1_name, u1.profile_image_url as user1_image,
                u2.name as user2_name, u2.profile_image_url as user2_image
                FROM chats c
                LEFT JOIN users u1 ON c.user1_id = u1.id
                LEFT JOIN users u2 ON c.user2_id = u2.id
                WHERE c.id = ?";
    $get_stmt = $conn->prepare($get_sql);
    $get_stmt->bind_param("s", $chat_id);
    $get_stmt->execute();
    $chat_result = $get_stmt->get_result();
    $chat_data = $chat_result->fetch_assoc();
    
    $otherUserId = $chat_data['user1_id'] === $data->user1_id ? $chat_data['user2_id'] : $chat_data['user1_id'];
    $otherUserName = $chat_data['user1_id'] === $data->user1_id ? $chat_data['user2_name'] : $chat_data['user1_name'];
    $otherUserImage = $chat_data['user1_id'] === $data->user1_id ? $chat_data['user2_image'] : $chat_data['user1_image'];
    
    sendResponse([
        'success' => true,
        'chat' => [
            'id' => $chat_id,
            'userId' => $data->user1_id,
            'otherUserId' => $otherUserId,
            'otherUserName' => $otherUserName,
            'otherUserImageUrl' => $otherUserImage,
            'lastMessage' => null,
            'lastMessageTime' => null,
            'unreadCount' => 0,
            'createdAt' => strtotime($chat_data['created_at']) * 1000
        ]
    ]);
} else {
    sendResponse(['error' => 'Failed to create chat'], 500);
}

$stmt->close();
$get_stmt->close();
$conn->close();

?>

