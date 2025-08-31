@echo off
REM Complete App Cache and Data Clearing Script
echo ====================================
echo CLEARING CAMPUS APP CACHE & DATA
echo ====================================

set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
set ADB=%ANDROID_HOME%\platform-tools\adb.exe
set PACKAGE_NAME=com.group.campus

echo.
echo Step 1: Checking connected devices...
"%ADB%" devices

echo.
echo Step 2: Stopping the Campus app...
"%ADB%" shell am force-stop %PACKAGE_NAME%

echo.
echo Step 3: Clearing app cache...
"%ADB%" shell pm clear %PACKAGE_NAME%

echo.
echo Step 4: Clearing shared preferences...
"%ADB%" shell "run-as %PACKAGE_NAME% rm -rf shared_prefs/"

echo.
echo Step 5: Clearing internal storage...
"%ADB%" shell "run-as %PACKAGE_NAME% rm -rf files/"
"%ADB%" shell "run-as %PACKAGE_NAME% rm -rf cache/"

echo.
echo Step 6: Clearing Firebase Auth cache...
"%ADB%" shell "run-as %PACKAGE_NAME% rm -rf databases/"

echo.
echo ====================================
echo CACHE CLEARING COMPLETE!
echo ====================================
echo The app cache, login data, and all stored
echo preferences have been cleared.
echo.
echo You can now:
echo 1. Launch the Campus app
echo 2. Go through onboarding again
echo 3. Login with any credentials
echo 4. Test the app fresh
echo ====================================

pause
