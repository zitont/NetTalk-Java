@echo off
echo Building NetTalk application...

REM Clean and package with Maven
call mvn clean package

echo.
echo Build completed!
echo.
echo Executable file is located at: target\NetTalk.exe
echo.
echo Press any key to exit...
pause > nul