@echo off
REM Internet Connectivity Fix for Android Emulator
echo ====================================
echo FIXING EMULATOR INTERNET CONNECTIVITY
echo ====================================

set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
set ADB=%ANDROID_HOME%\platform-tools\adb.exe

echo.
echo Step 1: Setting DNS servers to Google DNS...
"%ADB%" shell "su -c 'setprop net.dns1 8.8.8.8'"
"%ADB%" shell "su -c 'setprop net.dns2 8.8.4.4'"

echo.
echo Step 2: Restarting network services...
"%ADB%" shell "su -c 'stop'"
"%ADB%" shell "su -c 'start'"

echo.
echo Step 3: Testing internet connectivity...
"%ADB%" shell "ping -c 3 8.8.8.8"

echo.
echo ====================================
echo ALTERNATIVE SOLUTIONS:
echo ====================================
echo If the above doesn't work, try these in Android Studio:
echo.
echo 1. COLD BOOT EMULATOR:
echo    - Tools ^> AVD Manager
echo    - Click dropdown arrow next to your emulator
echo    - Choose "Cold Boot Now"
echo.
echo 2. EMULATOR EXTENDED CONTROLS:
echo    - Click "..." (more) in emulator toolbar
echo    - Go to Settings ^> Proxy
echo    - Select "Use Android Studio HTTP proxy settings"
echo.
echo 3. NETWORK SETTINGS:
echo    - Extended Controls ^> Cellular ^> Network type
echo    - Change to "LTE" or "GSM"
echo    - Toggle airplane mode on/off
echo.
echo 4. DNS SETTINGS IN EMULATOR:
echo    - Open Settings app in emulator
echo    - Wi-Fi ^> Advanced ^> IP Settings
echo    - Change to Static
echo    - DNS 1: 8.8.8.8
echo    - DNS 2: 8.8.4.4
echo ====================================

pause
