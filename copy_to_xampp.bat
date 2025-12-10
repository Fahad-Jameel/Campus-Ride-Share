@echo off
REM Script to copy backend files to XAMPP (Windows)
REM Usage: copy_to_xampp.bat

echo Copying backend files to XAMPP...

set XAMPP_PATH=C:\xampp\htdocs\campus-ride-api

REM Create directory if it doesn't exist
if not exist "%XAMPP_PATH%" mkdir "%XAMPP_PATH%"

REM Copy all files
echo Copying files from backend\api\ to %XAMPP_PATH%...
xcopy /E /I /Y backend\api\* "%XAMPP_PATH%\"

REM Create uploads directories
echo Creating uploads directories...
if not exist "%XAMPP_PATH%\uploads" mkdir "%XAMPP_PATH%\uploads"
if not exist "%XAMPP_PATH%\uploads\profile" mkdir "%XAMPP_PATH%\uploads\profile"
if not exist "%XAMPP_PATH%\uploads\vehicle" mkdir "%XAMPP_PATH%\uploads\vehicle"
if not exist "%XAMPP_PATH%\uploads\ride" mkdir "%XAMPP_PATH%\uploads\ride"
if not exist "%XAMPP_PATH%\uploads\general" mkdir "%XAMPP_PATH%\uploads\general"

echo Done! Files copied to: %XAMPP_PATH%
echo.
echo Next steps:
echo 1. Start XAMPP (Apache and MySQL)
echo 2. Import database.sql in phpMyAdmin
echo 3. Import database_notifications.sql in phpMyAdmin
echo 4. Update config.php with your database credentials
echo 5. Update Firebase Server Key in notifications\send_fcm_notification.php
echo 6. Test API: http://localhost/campus-ride-api/config.php

pause

