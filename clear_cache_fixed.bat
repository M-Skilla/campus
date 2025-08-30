@echo off
REM Complete App Cache and Data Clearing Script - Fixed for Multiple Devices
echo ====================================
echo CLEARING CAMPUS APP CACHE & DATA
echo ====================================

set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
set ADB=%ANDROID_HOME%\platform-tools\adb.exe
set PACKAGE_NAME=com.group.campus
set DEVICE_ID=emulator-5554

echo.
echo Step 1: Targeting device: %DEVICE_ID%
"%ADB%" -s %DEVICE_ID% devices

echo.
echo Step 2: Stopping the Campus app...
"%ADB%" -s %DEVICE_ID% shell am force-stop %PACKAGE_NAME%

echo.
echo Step 3: Clearing app cache and data completely...
"%ADB%" -s %DEVICE_ID% shell pm clear %PACKAGE_NAME%

echo.
echo Step 4: Uninstalling app to ensure clean state...
"%ADB%" -s %DEVICE_ID% uninstall %PACKAGE_NAME%

echo.
echo Step 5: Clearing any remaining Firebase auth tokens...
"%ADB%" -s %DEVICE_ID% shell settings delete global firebase_auth_token 2>nul

echo.
echo ====================================
echo CACHE CLEARING COMPLETE!
echo ====================================
echo The app has been completely removed and all
echo cache/login data has been cleared.
echo.
echo Next steps:
echo 1. Reinstall the app
echo 2. Go through onboarding
echo 3. Login fresh with your credentials
echo ====================================

pause
