@echo off
setlocal enabledelayedexpansion
set NUM_SPLITS=%1
if "%NUM_SPLITS%"=="" set NUM_SPLITS=1
echo Starting %NUM_SPLITS% Chrome instances with remote debugging...
for /L %%i in (1,1,%NUM_SPLITS%) do (
    set /a port=9221 + %%i
    start "" "C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=!port! --user-data-dir="C:\chrome-debug%%i"
)
echo Waiting for Chrome to start...
timeout /t 10 /nobreak
echo Running the email sending tests...
cd /d "c:\Users\Darsh\Desktop\frmwrk"
mvn exec:java "-Dexec.mainClass=org.testng.TestNG" "-Dexec.args=testng.xml" "-DnumSplits=%NUM_SPLITS%"
echo Tests completed. Closing Chrome...
taskkill /f /im chrome.exe
echo Done.
pause