-- Create bookings table
CREATE TABLE IF NOT EXISTS `bookings` (
  `id` varchar(50) PRIMARY KEY,
  `ride_id` varchar(50) NOT NULL,
  `passenger_id` varchar(50) NOT NULL,
  `seats_requested` int NOT NULL,
  `stop_location` varchar(255) DEFAULT NULL,
  `status` enum('pending','accepted','rejected','cancelled') DEFAULT 'pending',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`ride_id`) REFERENCES `rides`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`passenger_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add indexes for better performance
CREATE INDEX idx_ride_id ON bookings(ride_id);
CREATE INDEX idx_passenger_id ON bookings(passenger_id);
CREATE INDEX idx_status ON bookings(status);
