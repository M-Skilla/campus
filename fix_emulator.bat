@echo off
REM Android Emulator Fix Script
REM This script helps fix common emulator issues

echo ====================================
echo ANDROID EMULATOR TROUBLESHOOTING
echo ====================================

echo.
echo Step 1: Finding Android SDK...
set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    echo ✓ Found Android SDK at: %ANDROID_HOME%
    set ADB=%ANDROID_HOME%\platform-tools\adb.exe
) else (
    echo ✗ Android SDK not found at default location
    echo Please check if Android Studio is properly installed
    pause
    exit /b 1
)

echo.
echo Step 2: Checking connected devices...
"%ADB%" devices

echo.
echo Step 3: Killing and restarting ADB server...
"%ADB%" kill-server
timeout /t 2 /nobreak >nul
"%ADB%" start-server

echo.
echo Step 4: Checking devices again...
"%ADB%" devices

echo.
echo ====================================
echo MANUAL STEPS TO COMPLETE:
echo ====================================
echo 1. If device shows as "unauthorized":
echo    - Look at your emulator screen
echo    - Accept the "Allow USB Debugging" dialog
echo    - Check "Always allow from this computer"
echo    - Click OK
echo.
echo 2. If no emulator is running:
echo    - Open Android Studio
echo    - Go to Tools ^> AVD Manager
echo    - Start your emulator
echo.
echo 3. For internet connectivity issues:
echo    - Use Google DNS: 8.8.8.8 and 8.8.4.4
echo    - Enable "Use Host GPU" in emulator settings
echo    - Try cold boot of emulator
echo ====================================

pause
