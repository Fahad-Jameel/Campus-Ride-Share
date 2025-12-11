<?php
// Simple test script to check if the API endpoint is working
// Place this in your XAMPP htdocs/campus-ride-api/ directory and access via browser

require_once __DIR__ . '/api/config.php';

try {
    $conn = getDBConnection();
    
    // Check if rides table exists and has data
    $check_table = "SHOW TABLES LIKE 'rides'";
    $result = $conn->query($check_table);
    
    if ($result->num_rows == 0) {
        echo "ERROR: 'rides' table does not exist in the database.<br>";
        echo "Please run the database.sql script to create the tables.<br>";
        exit;
    }
    
    // Count total rides
    $count_sql = "SELECT COUNT(*) as total FROM rides";
    $count_result = $conn->query($count_sql);
    $count_row = $count_result->fetch_assoc();
    $total_rides = $count_row['total'];
    
    echo "Total rides in database: $total_rides<br><br>";
    
    if ($total_rides == 0) {
        echo "WARNING: No rides found in the database.<br>";
        echo "This is why the app is not showing any rides.<br>";
        echo "Please create some rides using the 'Offer a Ride' feature in the app.<br>";
    } else {
        // Get sample rides
        $sample_sql = "SELECT r.*, u.name as driver_name 
                      FROM rides r 
                      JOIN users u ON r.driver_id = u.id 
                      ORDER BY r.created_at DESC 
                      LIMIT 5";
        $sample_result = $conn->query($sample_sql);
        
        echo "Sample rides (latest 5):<br>";
        echo "<table border='1' cellpadding='5'>";
        echo "<tr><th>ID</th><th>Driver</th><th>Pickup</th><th>Destination</th><th>Date</th><th>Time</th><th>Available Seats</th></tr>";
        
        while ($row = $sample_result->fetch_assoc()) {
            echo "<tr>";
            echo "<td>" . htmlspecialchars($row['id']) . "</td>";
            echo "<td>" . htmlspecialchars($row['driver_name']) . "</td>";
            echo "<td>" . htmlspecialchars($row['pickup_location']) . "</td>";
            echo "<td>" . htmlspecialchars($row['destination']) . "</td>";
            echo "<td>" . htmlspecialchars($row['date']) . "</td>";
            echo "<td>" . htmlspecialchars($row['time']) . "</td>";
            echo "<td>" . $row['available_seats'] . "/" . $row['total_seats'] . "</td>";
            echo "</tr>";
        }
        echo "</table><br>";
    }
    
    // Test the API endpoint response format
    echo "<br>Testing API endpoint format:<br>";
    $test_sql = "SELECT r.*, u.name as driver_name, u.profile_image_url as driver_image_url 
                 FROM rides r 
                 JOIN users u ON r.driver_id = u.id 
                 ORDER BY r.created_at DESC 
                 LIMIT 3";
    $test_result = $conn->query($test_sql);
    
    $rides = [];
    while ($row = $test_result->fetch_assoc()) {
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
    
    $response = ['success' => true, 'rides' => $rides];
    echo "<pre>" . json_encode($response, JSON_PRETTY_PRINT) . "</pre>";
    
    $conn->close();
    
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "<br>";
    echo "Please check your database configuration in api/config.php<br>";
}
?>

