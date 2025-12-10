#!/bin/bash

# Script to copy backend files to XAMPP
# Usage: ./copy_to_xampp.sh

echo "Copying backend files to XAMPP..."

# Detect OS and set XAMPP path
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    XAMPP_PATH="/Applications/XAMPP/htdocs/campus-ride-api"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    XAMPP_PATH="/opt/lampp/htdocs/campus-ride-api"
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    # Windows (Git Bash)
    XAMPP_PATH="/c/xampp/htdocs/campus-ride-api"
else
    echo "Unknown OS. Please copy files manually."
    exit 1
fi

# Create directory if it doesn't exist
mkdir -p "$XAMPP_PATH"

# Copy all files
echo "Copying files from backend/api/ to $XAMPP_PATH..."
cp -r backend/api/* "$XAMPP_PATH/"

# Create uploads directories
echo "Creating uploads directories..."
mkdir -p "$XAMPP_PATH/uploads/profile"
mkdir -p "$XAMPP_PATH/uploads/vehicle"
mkdir -p "$XAMPP_PATH/uploads/ride"
mkdir -p "$XAMPP_PATH/uploads/general"

# Set permissions (Mac/Linux)
if [[ "$OSTYPE" != "msys" && "$OSTYPE" != "win32" ]]; then
    echo "Setting permissions..."
    chmod -R 755 "$XAMPP_PATH/uploads"
fi

echo "Done! Files copied to: $XAMPP_PATH"
echo ""
echo "Next steps:"
echo "1. Start XAMPP (Apache and MySQL)"
echo "2. Import database.sql in phpMyAdmin"
echo "3. Import database_notifications.sql in phpMyAdmin"
echo "4. Update config.php with your database credentials"
echo "5. Update Firebase Server Key in notifications/send_fcm_notification.php"
echo "6. Test API: http://localhost/campus-ride-api/config.php"

